package com.sqshq.facets.model

import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import java.util.*

object Converter {
    fun convertToTaxonomy(original: com.sqshq.facets.model.Document, type: FieldType): Document {
        val result = Document()
        result.add(StringField("id", UUID.randomUUID().toString(), Field.Store.YES))
        result.add(type.create("city", original.data.city))
        result.add(type.create("neighbourhood", original.data.neighbourhood))
        result.add(type.create("name", original.data.name))
        result.add(type.create("country", original.data.country))
        result.add(type.create("location", original.data.location))
        result.add(type.create("lastReview", original.data.lastReview))
        result.add(type.create("updatedDate", original.data.updatedDate))
        result.add(type.create("roomType", original.data.roomType))
        result.add(type.create("hostLanguage", original.data.hostLanguage))
        result.add(type.create("hostType", original.data.hostType))
        result.add(type.create("listingType", original.data.listingType))
        result.add(type.create("cancellationPolicy", original.data.cancellationPolicy))
        result.add(type.create("bookingRequirements", original.data.bookingRequirements))
        result.add(type.create("legal.ownershipType", original.data.legal.ownershipType))
        original.data.highlights.forEach { result.add(type.create("highlights", it)) }
        original.data.houseRules.forEach { result.add(type.create("houseRules", it)) }
        original.data.healthSafety.forEach { result.add(type.create("healthSafety", it)) }
        original.data.amenities.general.forEach { result.add(type.create("amenities.general", it)) }
        original.data.amenities.bathroom.forEach {
            result.add(
                type.create(
                    "amenities.bathroom",
                    it
                )
            )
        }
        original.data.amenities.bedroom.forEach { result.add(type.create("amenities.bedroom", it)) }
        original.data.amenities.outdoor.forEach { result.add(type.create("amenities.outdoor", it)) }
        original.data.amenities.accessibility.forEach {
            result.add(
                type.create(
                    "amenities.accessibility",
                    it
                )
            )
        }
        original.data.reviewsTopCategories.forEach {
            result.add(
                type.create(
                    "reviewsTopCategories",
                    it
                )
            )
        }
        original.data.locationTags.forEach { result.add(type.create("locationTags", it)) }
        original.data.designTags.forEach { result.add(type.create("designTags", it)) }
        original.data.houseRules.forEach { result.add(type.create("houseRules", it)) }
        return result
    }
}