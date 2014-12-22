/**
 *
 */
package com.idyria.osi.aib.core.service

/**
 *
 * A Service Group groups services together
 * It is the base component to build a system with more than one service
 *
 * All Services are created in a separated classloader, giving them separated AIB busses
 * If some services should be started under the same hood, this has to be specified using the special "virtual" construct.
 *
 * Usage is as following:
 *
 * var services = new ServiceGroup() {
 *
 * 		service { service =>
 *
 * 		}
 *
 *
 *
 * }
 *
 *
 * @author rleys
 *
 */
class ServiceGroup ( final var name : String ) extends ServiceBuilder {


	// Stack as group





}
