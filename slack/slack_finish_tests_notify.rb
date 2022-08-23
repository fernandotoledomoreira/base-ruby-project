require_relative "../features/mocks/slack_report.rb"
require 'json'
require 'httparty'

if ENV['config_vars'].include?('hml')
  ENV['CONFIG_ENV'] = 'hml'
elsif ENV['config_vars'].include?('dev')
  ENV['CONFIG_ENV'] = 'dev'
elsif ENV['config_vars'].include?('prd')
  ENV['CONFIG_ENV'] = 'prd'
end

system('allure generate reports/allure-results')
report_uri = "https://report-tests.devtools.caradhras.io/#{ENV['JOB_BASE_NAME']}/#{ENV['CONFIG_ENV']}/#{ENV['BUILD_ID']}/index.html"
uri_lib = "uri_incoming_webhook"
file = JSON.parse(File.read('allure-report/history/history-trend.json'))
results = JSON.pretty_generate(file[0]['data'])

payload = slack_report(report_uri, file[0]['data'], results)
options = { body: payload.to_json, headers: { "Content-type": "application/json" } }
HTTParty.post(uri_lib, options)

