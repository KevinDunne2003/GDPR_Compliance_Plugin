package com.github.kevindunne2003.gdprcomplianceplugin.startup
data class codeType(val name: String, val typeOfDataComparisonFound: Int, val indexOfDataComparison: Int)

class CatalogueMatcher {
    //Case insensitive checks are used here
    private var varNamesThatMatchDataCat = mutableListOf<codeType>()
    private var methodNamesThatMatchDataCat = mutableListOf<codeType>()
    private var classNamesThatMatchDataCat = mutableListOf<codeType>()


    fun compareDataCatKeywordsWithVariableNames(){
        for(i in 0 until DataCatalogueLoader.getDataCatKeywords().size){
            for(j in 0 until CodeScanningEngine.getVariableNamesInsideProject().size){
                if(DataCatalogueLoader.getDataCatKeywords().get(i).equals(CodeScanningEngine.getVariableNamesInsideProject().get(j), ignoreCase = true)){
                    varNamesThatMatchDataCat.add(codeType(CodeScanningEngine.getVariableNamesInsideProject().get(j),1,i))
                }
            }
        }
    }

    fun compareDataCatKeywordsWithMethodNames(){
        for(i in 0 until DataCatalogueLoader.getDataCatKeywords().size){
            for(j in 0 until CodeScanningEngine.getMethodNamesInsideProject().size){
                if(DataCatalogueLoader.getDataCatKeywords().get(i).equals(CodeScanningEngine.getMethodNamesInsideProject().get(j), ignoreCase = true)){
                    methodNamesThatMatchDataCat.add(codeType(CodeScanningEngine.getMethodNamesInsideProject().get(j),1,i))
                }
            }
        }
    }

    fun compareDataCatKeywordsWithClassNames(){
        for(i in 0 until DataCatalogueLoader.getDataCatKeywords().size){
            for(j in 0 until CodeScanningEngine.getClassNamesInsideProject().size){
                if(DataCatalogueLoader.getDataCatKeywords().get(i).equals(CodeScanningEngine.getClassNamesInsideProject().get(j), ignoreCase = true)){
                    classNamesThatMatchDataCat.add(codeType(CodeScanningEngine.getClassNamesInsideProject().get(j),1,i))
                }
            }
        }
    }

    fun compareDataCatRelatedTermsWithClassNames(){
        for(i in 0 until DataCatalogueLoader.getDataCatRelatedTerms().size){
            for(j in 0 until CodeScanningEngine.getClassNamesInsideProject().size){
                if(DataCatalogueLoader.getDataCatRelatedTerms().get(i).equals(CodeScanningEngine.getClassNamesInsideProject().get(j), ignoreCase = true)){
                    classNamesThatMatchDataCat.add(codeType(CodeScanningEngine.getClassNamesInsideProject().get(j),2,i))
                }
            }
        }
    }


}