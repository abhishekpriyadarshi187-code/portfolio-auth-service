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

        stage('Build Docker Image') {
            steps {
                script {
                    def shortCommit = env.GIT_COMMIT.take(7)
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                }

                sh '''
                    echo "Building Docker image: portfolio-auth-service:${IMAGE_TAG}"
                    docker build \
                      -t portfolio-auth-service:${IMAGE_TAG} \
                      .
                '''
            }
        }
    }
}
