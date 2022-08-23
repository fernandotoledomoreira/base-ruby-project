def slack_report(uri, file, results)
    file["failed"].to_i > 0 ? color = '#ff0033' : color = '#00FF00'
    {
      "username": "Report Automation Tests",
      "icon_url": "",
      "attachments": [
        {
          "fallback": "Required plain-text summary of the attachment.",
          "color": color,
          "author_link": "",
          "author_icon": "",
          "text": "<!channel> \n *Tests Executeds!* \n *Pipeline Name:* #{JOB_BASE_NAME} \n *Enviroment: #{CONFIG}* \n *Report Link:* #{uri} \n",
          "fields": [
            {
              "title": "Results:",
              "value": "``` #{results} ```",
              "short": true
            }
          ],
          "image_url": "",
          "thumb_url": "",
          "footer": "QA Team",
          "footer_icon": "",
          "ts": Time.now.to_i
        }
      ]
    }
end

def starting_pipeline(job_name, build_id)
  {
    "username": "redhorn-people-tests",
    "icon_url": "",
    "attachments": [
      {
        "fallback": "",
        "color": "#FFFF00",
        "author_link": "",
        "author_icon": "",
        "text": "<!here> *Starting Pipeline* :loading: \n *Console:* \n http://jenkins.dock.tech:8080/job/#{job_name}/#{build_id}/consoleFull \n",
        "image_url": "",
        "thumb_url": "",
        "footer": "QA Team",
        "footer_icon": "",
        "ts": Time.now.to_i
      }
    ]
  }
end

