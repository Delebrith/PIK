pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                echo 'Building...'
                sh './gradlew assemble'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
                sh './gradlew check'
                sh 'npm install'
                karma start
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'
                }
            }
        }
        stage('Deploy snaphot'){
            when { branch 'develop'}
            steps {
                sh './gradlew war'
                echo 'Deploying ${env.BRANCH_NAME} to snapshots...'
                sh './gradlew uploadArchives'
            }
        }
        stage('Release') {
            when { branch 'master'}
            steps {
                sh './gradlew war'
                echo 'Deploying ${env.BRANCH_NAME} to releases...'
                sh './gradlew uploadArchives'
            }
        }
    }
    post {
        unstable {
            mail to: "llepak@mion.elka.pw.edu.pl",
                    subject: "Unstable Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is not yes with ${env.BUILD_URL}"
        }
        failure {
            mail to: "llepak@mion.elka.pw.edu.pl",
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is not yes with ${env.BUILD_URL}"
        }
    }
}