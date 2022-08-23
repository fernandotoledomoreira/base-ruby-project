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
