pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Jenkins Pipeline') {
            steps {
                echo 'Jenkins pipeline is running successfully for portfolio-auth-service'
                sh 'java -version'
                sh './mvnw -version'
            }
        }
    }
}
