package com.github.kevindunne2003.gdprcomplianceplugin.startup.ReportGenerators

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis.CatalogueMatchFilter
import com.github.kevindunne2003.gdprcomplianceplugin.startup.DataCatalogueValues.DataCatalogueRepository
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.DataCatalogueType
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.Principles
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ViolationData
import com.github.kevindunne2003.gdprcomplianceplugin.startup.Objects.ViolationPrincipleMatch

object ViolationPrincipleMapper {

    private var variableViolationsMappedToPrinciples: MutableList<ViolationPrincipleMatch> = mutableListOf()
    private var fieldViolationsMappedToPrinciples: MutableList<ViolationPrincipleMatch> = mutableListOf()
    private var parameterViolationsMappedToPrinciples: MutableList<ViolationPrincipleMatch> = mutableListOf()

    fun mapAllViolationsToPrinciples() {
        mapAllLocalVarsToPrinciples()
        mapAllFieldViolationsToPrinciples()
        mapAllParamVarsToPrinciples()
    }

    private fun mapAllFieldViolationsToPrinciples() {
        val filteredFieldVariablesAndMethodsThatContainViolations = CatalogueMatchFilter.getFilteredFieldVariablesAndMethodsThatContainViolations()
        for(entries in filteredFieldVariablesAndMethodsThatContainViolations){
            val principlesMatches = mapViolationDataToPrinciple(entries)
            if(principlesMatches != null) {
                fieldViolationsMappedToPrinciples.add(ViolationPrincipleMatch(principlesMatches, entries))
            }
        }
    }

    private fun mapAllLocalVarsToPrinciples() {
        val filteredLocalVariablesAndMethodsThatContainViolations =
            CatalogueMatchFilter.getFilteredLocalVariablesAndMethodsThatContainViolations()
        for(entries in filteredLocalVariablesAndMethodsThatContainViolations) {
            val principlesMatches = mapViolationDataToPrinciple(entries)
            if(principlesMatches != null) {
                variableViolationsMappedToPrinciples.add(ViolationPrincipleMatch(principlesMatches, entries))
            }
        }
    }

    private fun mapAllParamVarsToPrinciples() {
        val filteredParamVariablesAndMethodsThatContainViolations =
            CatalogueMatchFilter.getFilteredParamVariablesAndMethodsThatContainViolations()
        for (entries in filteredParamVariablesAndMethodsThatContainViolations) {
            val principleMatches = mapViolationDataToPrinciple(entries)
            if (principleMatches != null) {
                parameterViolationsMappedToPrinciples.add(ViolationPrincipleMatch(principleMatches, entries))
            }

        }

    }


    private fun mapViolationDataToPrinciple(violationData: ViolationData): MutableList<Principles>? {
        val methodCompData = violationData.comparisonDataOfMethod
        var keyword: String?

        if (methodCompData.typeOfDataCatVal == DataCatalogueType.Keyword) {
            keyword = methodCompData.dataCatName
        } else {
            keyword = mapRelatedTermToKeyword(methodCompData.row)
        }

        if (keyword != null) {
            val listOfMappedViolationToPrinciple = mapStringRepToPrinciple(keyword)
            return listOfMappedViolationToPrinciple
        }

        return null
    }

    private fun mapStringRepToPrinciple(stringRep: String): MutableList<Principles> {
        var principles = mutableListOf<Principles>()

        if (stringRep == "create" || stringRep == "add" || stringRep == "save") {
            principles.add(Principles.LawfulnessFairnessAndTransparency)
            principles.add(Principles.PurposeLimitations)
        } else if (stringRep == "view") {
            principles.add(Principles.IntegrityAndConfidentiality)
        } else if (stringRep == "edit" || stringRep == "change") {
            principles.add(Principles.Accuracy)
        } else if (stringRep == "delete") {
            principles.add(Principles.StorageLimitations)
            principles.add(Principles.DataMinimisation)
        } else {
            println("error")
            println(stringRep)
        }

        return principles
    }

    private fun mapRelatedTermToKeyword(rowNum: Int?): String? {
        if (rowNum != null) {
            var keywords = DataCatalogueRepository.getDataCatalogueKeywords()

            return keywords.get(rowNum)
        }
        return null
    }

    fun getVariableViolationsMappedToPrinciples(): MutableList<ViolationPrincipleMatch> {
        return variableViolationsMappedToPrinciples
    }

    fun getFieldViolationsMappedToPrinciples(): MutableList<ViolationPrincipleMatch> {
        return fieldViolationsMappedToPrinciples
    }

    fun getParamViolationsMappedToPrinciples(): MutableList<ViolationPrincipleMatch> {
        return parameterViolationsMappedToPrinciples
    }
}