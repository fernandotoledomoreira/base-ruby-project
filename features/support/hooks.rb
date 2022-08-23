Before do |scenario|
  File.new("evidence.log", "w")
  @log = QaServices::LogService.new
  token_service = QaServices::TokenService.new
  uri_auth = 'https://auth2.dev.caradhras.io/token?grant_type=client_credentials' if ENV_RUN.include?('_dev')
  uri_auth = 'https://auth2.hml.caradhras.io/token?grant_type=client_credentials' if ENV_RUN.include?('_hml')
  @token = token_service.create_token(uri_auth, ENV['username'], ENV['password'])
end

After do
  @report_log = File.read("evidence.log")
  Allure.add_attachment(name: 'Evidence', source: "#{@report_log}", type: Allure::ContentType::TXT, test_case: true)
  File.delete("evidence.log")
end

at_exit do
  if DEVOPS
    system('allure generate reports/allure-results')
    report_uri = "https://report-tests.devtools.caradhras.io/#{JOB_BASE_NAME}/#{CONFIG}/#{BUILD_ID}/index.html"
    uri_channel = '' # URL do webhook
    file = JSON.parse(File.read('allure-report/history/history-trend.json'))
    results = JSON.pretty_generate(file[0]["data"])

    payload = slack_report(report_uri, file[0]["data"], results)
    params = {
      "uri": uri_channel,
      "headers": { "Content-type": "application/json" },
      "body": payload.to_json
    }
    # QaServices::ApiRequest.new.execute_request('POST', params, true)
  end
  final_env = ({ "Execution" => ENV_RUN })
  final_env = final_env.merge(VERSION_TEST)
  Allure.add_environment(final_env)
end