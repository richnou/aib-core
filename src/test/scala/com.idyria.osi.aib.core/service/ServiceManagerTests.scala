package com.idyria.osi.aib.core.service


import org.scalatest._

class ServiceManagerTest extends FeatureSpec  {

    class TestService extends Service("TestService") {

    }


    feature("Build Services") {


        object services extends ServicesManager {

            service("A Service") { s=>

              service("Sub a") { s => }

            }

            service("Another Service") { s=>


              service("Sub b") { s => }

            }
            service (new TestService())






          }

    }


}
