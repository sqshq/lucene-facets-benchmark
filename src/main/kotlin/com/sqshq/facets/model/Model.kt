package com.sqshq.facets.model

import com.google.gson.annotations.SerializedName

data class Document(
    val data: Data
) {
    fun getStringFields(): Map<String, String> {
        return mapOf(
            "city" to data.city,
            "neighbourhood" to data.neighbourhood,
            "name" to data.name,
            "country" to data.country,
            "location" to data.location,
            "lastReview" to data.lastReview,
            "updatedDate" to data.updatedDate,
            "roomType" to data.roomType,
            "hostLanguage" to data.hostLanguage,
            "hostType" to data.hostType,
            "cancellationPolicy" to data.cancellationPolicy,
            "bookingRequirements" to data.bookingRequirements,
            "listingType" to data.listingType,
            "legal.ownershipType" to data.legal.ownershipType
        )
    }

    fun getArrayFields(): Map<String, List<String>> {
        return mapOf(
            "houseRules" to data.houseRules,
            "healthSafety" to data.healthSafety,
            "amenities.general" to data.amenities.general,
            "amenities.bathroom" to data.amenities.bathroom,
            "amenities.bedroom" to data.amenities.bedroom,
            "amenities.outdoor" to data.amenities.outdoor,
            "amenities.accessibility" to data.amenities.accessibility,
            "reviewsTopCategories" to data.reviewsTopCategories,
            "highlights" to data.highlights,
            "locationTags" to data.locationTags,
            "designTags" to data.designTags
        )
    }
}

data class Data(
    val city: String,
    val neighbourhood: String,
    val name: String,
    val country: String,
    val location: String,
    @SerializedName("last_review")
    val lastReview: String,
    @SerializedName("room_type")
    val roomType: String,
    @SerializedName("updated_date")
    val updatedDate: String,
    @SerializedName("cancellation_policy")
    val cancellationPolicy: String,
    @SerializedName("listing_type")
    val listingType: String,
    @SerializedName("host_type")
    val hostType: String,
    @SerializedName("booking_requirements")
    val bookingRequirements: String,
    @SerializedName("host_language")
    val hostLanguage: String,
    val legal: Legal,
    val amenities: Amenities,
    @SerializedName("location_tags")
    val locationTags: List<String>,
    @SerializedName("design_tags")
    val designTags: List<String>,
    @SerializedName("house_rules")
    val houseRules: List<String>,
    @SerializedName("health_safety")
    val healthSafety: List<String>,
    @SerializedName("reviews_top_categories")
    val reviewsTopCategories: List<String>,
    val highlights: List<String>
)

data class Legal(
    @SerializedName("ownership_type")
    val ownershipType: String
)

data class Amenities(
    val general: List<String>,
    val bathroom: List<String>,
    val bedroom: List<String>,
    val outdoor: List<String>,
    val accessibility: List<String>
)
