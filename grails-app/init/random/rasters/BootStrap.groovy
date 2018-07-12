package random.rasters

import org.geotools.factory.Hints

class BootStrap
{
	def randomDataService
	
	def init = { servletContext ->
		Hints.putSystemDefault( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE )
		
//		Thread.start {
//			randomDataService.createRandomRasterDataSet(1000000)
//		}
	}
	def destroy = {
	}
}
