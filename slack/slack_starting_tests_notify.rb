require_relative "../features/mocks/slack_report.rb"
require 'json'
require 'httparty'

payload = starting_pipeline(ENV['JOB_BASE_NAME'], ENV['BUILD_ID']).to_json
options = { body: payload, headers: { "Content-type": "application/json" } }
HTTParty.post('uri_incoming_webhook', options)

