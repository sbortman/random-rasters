package random.rasters

import geoscript.GeoScript
import geoscript.geom.MultiPolygon
import omar.raster.RasterDataSet
import geoscript.geom.Point
import org.apache.commons.io.FilenameUtils

class RandomDataService
{
	static final def GMT = TimeZone.getTimeZone( 'GMT' )
	
	static final def CARS = [
		FORD   : [
			'FESTIVO',
			'MUSTANG',
			'FOCUS',
			'TARUS'
		],
		CHEVY  : [
			'CAMARO',
			'IMPALA',
			'VOLT',
		],
		PONTIAC: [
			'TRANS-AM',
			'FIREBIRD',
			'SUN-FIRE',
		],
		TOYOTA : [
			'CAMRY',
			'COROLLA',
			'AVALON',
			'4-RUNNER'
		],
		HONDA  : [
			'CR-X',
			'ACCORD',
			'CIVIC'
		]
	]
	
	static final def rng = new Random()
	
	def messageSource
	
	
	def createRandomMissionId()
	{
		def set = CARS.keySet()
		
		return set[rng.nextInt( set.size() )]
	}
	
	def createRandomSensorId( def missionId )
	{
		def set = CARS[missionId]
		
		return set[rng.nextInt( set.size() )]
	}
	
	def createRandomAcquisitionDate()
	{
		def gc = Calendar.getInstance( GMT, Locale.default )
		def year = rng.nextInt( 9 ) + 2010
		
		gc.set( gc.YEAR, year )
		gc.set( gc.DAY_OF_YEAR, rng.nextInt( gc.getActualMaximum( gc.DAY_OF_YEAR ) ) + 1 )
		gc.set( gc.HOUR_OF_DAY, rng.nextInt( 23 ) )
		gc.set( gc.MINUTE, rng.nextInt( 59 ) )
		gc.set( gc.SECOND, rng.nextInt( 59 ) )
		
		gc.time.toTimestamp()
	}
	
	def createRandomGroundGeom()
	{
		def lon = rng.nextDouble() * 360 - 180
		def lat = rng.nextDouble() * 180 - 90
		
		def degrees = rng.nextDouble() * 360
		def radians = degrees * Math.PI / 180
		
		def g = GeoScript.unwrap(
			new MultiPolygon(
				new Point( lon, lat ).buffer( 1 ).bounds.polygon.rotate( radians, lon, lat )
			)
		)
		
		g.setSRID( 4326 )
		g
	}
	
	
	def createRandomRasterDataSet( def totalCount = 1 )
	{
		def file = '/data/space_coast_metric_private/3V050726P0000820271A0100007003410_00574200.ntf' as File
		def info = "oms-image-info ${ file.absolutePath }".execute().text
		def oms = new XmlSlurper().parseText( info )
		
		for ( def x = 1; x <= totalCount; x++ )
		{
			RasterDataSet.withTransaction {
				
				oms?.dataSets?.RasterDataSet?.collect { rasterDataSetNode ->
					def rasterDataSet = RasterDataSet.initRasterDataSet( rasterDataSetNode )
					def filename

//				println rasterDataSet
					
					rasterDataSet?.rasterEntries?.each { def rasterEntry ->
						rasterEntry.missionId = createRandomMissionId()
						rasterEntry.sensorId = createRandomSensorId( rasterEntry.missionId )
						rasterEntry.acquisitionDate = createRandomAcquisitionDate()
						rasterEntry.groundGeom = createRandomGroundGeom()
						
						def tempName = File.createTempFile( rng.nextInt( 1000 ).toString().padLeft( 3, '0' ), '' )
						
						filename = "${ rasterEntry.acquisitionDate.format( "'/'yyyy'/'MM'/'dd'/'HH'/'mm" ) }/${ rasterEntry.missionId }/${ rasterEntry.sensorId }${ tempName }.ntf"
						rasterEntry.filename = filename
						rasterEntry.indexId = filename.encodeAsSHA256()
						
						rasterEntry.fileObjects.each { def fileObj ->
							def ext = FilenameUtils.getExtension( fileObj.name )
							fileObj.name = "${ rasterEntry.filename }.${ ext }"
						}
					}
					
					rasterDataSet.fileObjects.each { def fileObj ->
						def ext = FilenameUtils.getExtension( fileObj.name )
						fileObj.name = "${ filename }.${ ext }"
					}
					
					if ( !rasterDataSet.save() )
					{
						rasterDataSet?.errors?.allErrors?.each {
							log.error messageSource.getMessage( it, Locale.default )
						}
					}
				}
				
				if ( x % 1000 == 0 )
				{
					log.info "count: ${ x }"
				}
			}
//        println info
		}
	}
}
