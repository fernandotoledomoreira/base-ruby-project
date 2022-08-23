pipeline {
  agent {
    docker {
      image "${params.cucumber_docker_image}"
      args '-v /var/jenkins_home:/var/jenkins_home -v /var/lib/jenkins:/var/lib/jenkins'
    }
  }
  triggers {
          cron('0 9 * * 1-5')
    }
  parameters {
    string(
      name: 'cucumber_docker_image',
      defaultValue:'758526784474.dkr.ecr.us-east-1.amazonaws.com/ruby-qa-automation-backend-base:latest',
      description: 'The cucumber docker image'
    )
    string(
      name: 'retry',
      defaultValue:'--retry 0',
      description: 'set quantity of retry if cucumber scenario failed'
    )
    string(
      name: 'config_vars',
      defaultValue:'cdt_hml',
      description: 'Where you set config vars'
    )
    string(
      name: 'cucumber_tag',
      defaultValue:'@make_all',
      description: 'Where you set cucumber tag'
    )
    string(
      name: 'cucumber_parallels_tag',
      defaultValue:'@prl_tests',
      description: 'Where you set cucumber tag'
    )
    string(
      name: 'env',
      defaultValue:'hml',
      description: 'Where you set enviroment (dev|hml|prd|dr)'
    )
  }
    stages {
      stage('CLEANING WORKDIR') {
        steps {
            deleteDir()
        }
      }
      stage('Slack Report Starting Pipeline'){
       steps { sh 'cd /ruby-qa-automation-backend-base && ruby slack/slack_starting_tests_notify.rb' }
      }
      stage('RUNNING PARALLELS CUCUMBER TESTS') {
        steps {
          catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            configFileProvider([configFile(fileId: "${params.env}", variable:"${params.env}")]) {
              wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                sh "cd /ruby-qa-automation-backend-base && . \$${params.env} >> /dev/null && bundle exec parallel_cucumber features/ -n 17 -o '-t ${params.cucumber_parallels_tag}'"
              }
            }
          }
        }
      }
      stage('RUNNING JOURNEY CUCUMBER TESTS') {
        steps {
          catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
            slackSend(channel: "#reports-cucumber-tests", color: '#FFFF00', message: "Tests Started \n Tag Run: ${params.cucumber_tag} \n Image: ${params.cucumber_docker_image} \n Build Number: ${BUILD_NUMBER} \n Pipeline: ${JOB_NAME} \n Build Console: ${BUILD_URL}console")
            configFileProvider([configFile(fileId: "${params.env}", variable:"${params.env}")]) {
              wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                sh "cd /ruby-qa-automation-backend-base && . \$${params.env} >> /dev/null && bundle exec cucumber -t ${params.cucumber_tag} --color ${params.retry}"
              }
            }
          }
        }
      }
      stage('Slack Report Finished Pipeline'){
       steps {
         sh 'cd /ruby-qa-automation-backend-base && ruby slack/slack_finish_tests_notify.rb && rm -R allure-report'
        }
      }
    }
    post {
      always{
      echo "TESTS FINISHED"
        sh 'mkdir -p ${WORKSPACE}/allure-results && cp -R /ruby-qa-automation-backend-base/reports/allure-results/* ${WORKSPACE}/allure-results'
        sh 'mkdir -p ${WORKSPACE}/result_html && cp /ruby-qa-automation-backend-base/reports/html/cucumber-jornada.html ${WORKSPACE}/result_html'
        sh 'mkdir -p ${WORKSPACE}/result_json && cp /ruby-qa-automation-backend-base/reports/target/cucumber-jornada.json ${WORKSPACE}/result_json'
        archiveArtifacts artifacts: 'result_html/cucumber-jornada.html'
          allure([
          includeProperties: true,
          jdk: '',
          reportBuildPolicy: 'ALWAYS',
          results: [[path: 'allure-results']]
        ])
        sh 'mkdir allure-report-${JOB_NAME}-${BUILD_NUMBER}'
        sh 'cp ../../jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/archive/allure-report.zip allure-report-${JOB_NAME}-${BUILD_NUMBER}'
        sh 'cd allure-report-${JOB_NAME}-${BUILD_NUMBER} && unzip allure-report.zip'
        sh "aws s3 cp allure-report-${JOB_NAME}-${BUILD_NUMBER}/allure-report s3://report-tests/${JOB_NAME}/${params.env}/${BUILD_NUMBER} --recursive"
        script {
          if ("${currentBuild.result}" == "SUCCESS") {
          slackSend(channel: "#reports-cucumber-tests", color: "#00FF00", message: "Tests Finished \n Tag Run: ${params.cucumber_tag} \n Build Number: ${BUILD_NUMBER} \n Pipeline: ${JOB_NAME} \n Report Link: https://report-tests.devtools.caradhras.io/${JOB_NAME}/${params.env}/${BUILD_NUMBER}/index.html \n Pipeline Results: ${currentBuild.result}")
        } else if ("${currentBuild.result}" == "UNSTABLE") {
         slackSend(channel: "#reports-cucumber-tests", color: "#FF0000", message: "Tests Finished \n Tag Run: ${params.cucumber_tag} \n Build Number: ${BUILD_NUMBER} \n Pipeline: ${JOB_NAME} \n Report Link: https://report-tests.devtools.caradhras.io/${JOB_NAME}/${params.env}/${BUILD_NUMBER}/index.html \n Pipeline Results: ${currentBuild.result}")
        } else {
          slackSend(channel: "#reports-cucumber-tests", color: "", message: "Problem Tests \n Tag Run: ${params.cucumber_tag} \n Build Number: ${BUILD_NUMBER} \n Pipeline: ${JOB_NAME} \n Report Link: https://report-tests.devtools.caradhras.io/${JOB_NAME}/${params.env}/${BUILD_NUMBER}/index.html \n Pipeline Results: ${currentBuild.result}")
        }
        }
      }
    }
  }