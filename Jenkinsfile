pipeline {
    agent {
      label "jenkins-maven"
    }
    environment {
      DOCKER_REGISTRY     = 'docker.io'
      ORG                 = 'activiti'
      APP_NAME            = 'example-runtime-bundle'
      CHARTMUSEUM_CREDS   = credentials('jenkins-x-chartmuseum')
      RELEASE_VERSION     = jx_release_version()
      SNAPSHOT_VERSION    = maven_project_version()
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        environment {
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE      = "$PREVIEW_NAMESPACE".toLowerCase()
          VERSION           = "$SNAPSHOT_VERSION-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
        }
        steps {
          container('maven') {
			sh "echo VERSION=$VERSION"            
          
            sh "mvn versions:set -DnewVersion=$VERSION"
            
            // Let's build and test this
            sh "mvn install -DskipITs=false"

            // MAYBE: Let's deploy preview artifacts
            //sh 'mvn deploy -DskipTests'

            // Let's build and publish preview Docker image
            dir ("./$APP_NAME") {
	          retry(3) {  
                sh "skaffold build -f skaffold.yaml"
	          }
            }
            
            dir ("./$APP_NAME/charts/runtime-bundle") {
	          retry(5) {  
	            sh "make release"
	          }
	        }
            
			input "Pause"            
            
            // TODO: create preview environment, i.e. sh "make preview"
          }
        }
      }
      stage('Build Release') {
        when {
          branch 'develop'
        }
        environment {
          VERSION = "$RELEASE_VERSION"
        }
        steps {
          container('maven') {
			sh "echo VERSION=$VERSION"            
          
            // ensure we're not on a detached head
            sh "git checkout develop"
            
            // Let's set up git credentials
            sh "git config --global credential.helper store"
            sh "jx step git credentials"
            
            // so we can retrieve the version in later steps
            sh "echo $VERSION > VERSION"
            sh "mvn versions:set -DnewVersion=$VERSION"
            
            // Let's test it 
            sh "mvn clean install -DskipITs=false"

            dir ("./$APP_NAME/charts/runtime-bundle") {
	          retry(5) {  
	            sh "make tag"
	          }
            }
            
            sh 'mvn clean deploy -DskipTests'

            // Let's build and push Docker image
            dir ("./$APP_NAME") {
	          retry(3) {  
              	sh 'skaffold build -f skaffold.yaml'
	          }
            }

            dir ("./$APP_NAME/charts/runtime-bundle") {
	          retry(5){  
	            sh 'make release'
	          }
	        }
     
            retry(2) {
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies $VERSION"
              sh "rm -rf .updatebot-repos/"
              sh "sleep \$((RANDOM % 10))"
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies $VERSION"
            }
            
          }
        }
      }
      stage('Build Release from Tag') {
        when {
          tag '*RELEASE'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout $TAG_NAME"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$TAG_NAME > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          }
          container('maven') {
            sh '''
              mvn clean deploy -P !alfresco -P central
              '''

            sh 'export VERSION=`cat VERSION`'// && skaffold build -f skaffold.yaml'

            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            //sh "updatebot push"
            //sh "updatebot update"

            sh "echo pushing with update using version \$(cat VERSION)"

            sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
          }
        }
      }
    }
    post {
        always {
            cleanWs()
        }
    }
  }
  
  def jx_release_version() {
    container('maven') {
        return sh( script: "echo \$(jx-release-version)", returnStdout: true).trim()
    }
  }

  def maven_project_version() {
    container('maven') {
        return sh( script: "echo \$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout -f pom.xml)", returnStdout: true).trim()
    }
  }
  
  