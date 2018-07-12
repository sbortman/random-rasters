package random.rasters

import org.geotools.factory.Hints
import com.vividsolutions.jts.geom.Geometry
import geoscript.GeoScript
import grails.converters.JSON
import groovy.json.JsonSlurper

class BootStrap
{
	def randomDataService

	def init = { servletContext ->

		JSON.registerObjectMarshaller( Geometry ) {
		         def json = GeoScript.wrap( it ).geoJSON

		         new JsonSlurper().parseText( json )
		       }

		Hints.putSystemDefault( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE )

//		Thread.start {
//			randomDataService.createRandomRasterDataSet(1000000)
//		}
	}
	def destroy = {
	}
}
