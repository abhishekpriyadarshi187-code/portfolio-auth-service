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
    }
}
