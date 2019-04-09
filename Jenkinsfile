pipeline {
   agent {
      kubernetes {
              // Change the name of jenkins-maven label to be able to use yaml configuration snippet
              label "maven-dind"
              // Inherit from Jx Maven pod template
              inheritFrom "jenkins-maven-java11"
              // Add pod configuration to Jenkins builder pod template
              yamlFile "maven-dind.yaml"
      }
    }
    environment {
      ORG               = 'activiti'
      APP_NAME          = 'activiti-cloud-runtime-bundle-service'
      CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    }
    stages {
      stage('CI Build and push snapshot') {
        when {
          branch 'PR-*'
        }
        environment {
          PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
          PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
          HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        }
        steps {
          container('maven') {
            sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
            //add DskipTests since not clear how to fix test fast  
            //sh "mvn install"
            sh "mvn verify -B -DskipITs=false"
            sh "mvn install:install-file -Dfile=activiti-cloud-starter-runtime-bundle/swagger.json  -DgroupId=org.activiti.cloud.rb -DartifactId=swagger.json -Dclassifier=api-docs -Dversion=$PREVIEW_VERSION  -Dpackaging=json"
            sh "mvn deploy:deploy-file -P !alfresco -P central -Dfile=activiti-cloud-starter-runtime-bundle/swagger.json  -DgroupId=org.activiti.cloud.rb -DartifactId=swagger.json -Dclassifier=api-docs -Dversion=$PREVIEW_VERSION  -Dpackaging=json"
          }
        }
      }
      stage('Build Release') {
        when {
          branch 'develop'
        }
        steps {
          container('maven') {
            // ensure we're not on a detached head
            sh "git checkout develop"
            sh "git config --global credential.helper store"

            sh "jx step git credentials"
            // so we can retrieve the version in later steps
            sh "echo \$(jx-release-version) > VERSION"
            sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
            sh "mvn clean verify"

            retry(5){
              sh "git add --all"
              sh "git commit -m \"Release \$(cat VERSION)\" --allow-empty"
              sh "git tag -fa v\$(cat VERSION) -m \"Release version \$(cat VERSION)\""
              sh "git push origin v\$(cat VERSION)"
            }
          }
          container('maven') {
            sh 'mvn clean deploy -DskipTests'

            sh 'export VERSION=`cat VERSION`'

            sh "jx step git credentials"
            retry(2){
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
              sh "rm -rf .updatebot-repos/"
              sh "sleep \$((RANDOM % 10))"
              sh "updatebot push-version --kind maven org.activiti.cloud.rb:activiti-cloud-runtime-bundle-dependencies \$(cat VERSION)"
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
