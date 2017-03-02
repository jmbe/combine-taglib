#!groovy

node {
   
   stage 'Checkout'
   checkout scm

   stage 'Build'

   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.           
   def mvnHome = tool 'M3'

   sh "${mvnHome}/bin/mvn clean package"
   
   stage 'Deploy'
   sh "${mvnHome}/bin/mvn deploy"
}

