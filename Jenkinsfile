pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    stages {
        stage('Build and Verify') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('Verify Docker Access') {
            steps {
                sh 'docker --version'
                sh 'docker info'
            }
        }
    }
}
