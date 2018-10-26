pipeline {
  agent any
  tools {
    maven 'M3'
    jdk 'java-8-oracle'
  }

  stages {
    stage("Initialization") {
      steps {
        sh '''
              echo "PATH = ${PATH}"
              echo "M2_HOME = ${M2_HOME}"
              echo "Branch = ${BRANCH_NAME}"
          '''
      }
    }

    stage("Build") {
      steps {
        sh 'mvn -DskipTests clean package'
      }
    }

    stage("Test") {
      steps {
        sh 'mvn test'
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }
      }
    }

    stage("Deploy") {
      when {
        anyOf {
          branch "develop"
          branch "master"
        }
      }

      steps {
        sh 'mvn deploy'
      }
    }

  }
}
