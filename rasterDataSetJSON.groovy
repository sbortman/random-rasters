import omar.raster.RasterDataSet

RasterDataSet.withSession {
    println RasterDataSet.get(1).encodeAsJSON()
}