pipeline {
  agent {
    docker {
      image 'gizmotronic/oracle-java8'
      args '-v /root/.m2:/root/.m2 -u root'
    }

  }
  stages {
    stage('Build') {
      steps {
        sh 'apt-get update; apt-get install -y maven'
        sh 'mvn -s /var/jenkins_home/settings.xml -Dcss_repo=file:/var/jenkins_home/workspace/css_repo clean verify'
      }
    }
  }
}