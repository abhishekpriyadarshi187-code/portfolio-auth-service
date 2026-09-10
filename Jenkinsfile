pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    stages {
        stage('Verify Jenkins Pipeline') {
            steps {
                echo 'Jenkins pipeline is running successfully for portfolio-auth-service'
                sh 'java -version'
                sh './mvnw -version'
            }
        }
    }
}
