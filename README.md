# Lucene Facets Benchmark

Performance comparison between SSDVFF and Taxonomy implementations

### Config file format

```json5
{
  "mode": "INDEX",
  "indexType": "SSDVFF",
  "cycles": 5,
  "concurrency": 10,
  "enableMergeScheduler": true,
  "allFieldsMultivalue": false,
  "commitBuffer": 100000,
  "querySampleSize": 20,
  "queryPageSize": 20,
  "resourceFilePath": "/tmp/data/resource.json",
  "indexPath": "/tmp/data/lucene"
}
```

<details><summary>Example resource file</summary>
<p>

```json5
[{ 
    "_id" : ObjectId("606fc56504fc40425d1b225d"), 
    "geometry" : {
        "type" : "Point", 
        "coordinates" : [
            11.261314413194068, 
            43.779967029995476
        ]
    }, 
    "data" : {
        "updated_date" : "2020-06-19", 
        "city" : "Florence", 
        "calculated_host_listings_count" : NumberInt(1), 
        "reviews_per_month" : 2.42, 
        "minimum_nights" : NumberInt(2), 
        "number_of_reviews" : NumberInt(132), 
        "last_review" : "2020-01-29", 
        "host_id" : NumberInt(42112573), 
        "availability_365" : NumberInt(270), 
        "room_type" : "Entire home/apt", 
        "neighbourhood" : "Centro Storico", 
        "name" : "ELEGANT FLAT FLORENCE CITY CENTER", 
        "country" : "Italy", 
        "location" : "Italy, Florence, Centro Storico", 
        "price" : NumberInt(120), 
        "house_rules" : [
            "No smoking", 
            "No pets", 
            "No parties or events"
        ], 
        "health_safety" : [
            "Smoke alarm", 
            "AC", 
            "Heater"
        ], 
        "availability" : "HIGH", 
        "amenities" : {
            "general" : [
                "Air conditioning", 
                "Board games", 
                "Portable fans", 
                "Cooking basics", 
                "Dishwasher", 
                "Microwave", 
                "Washer", 
                "Barbecue utensils", 
                "Coffee maker", 
                "Stove", 
                "Heating", 
                "Private entrance", 
                "Hot water kettle", 
                "Pack 'n Play/travel crib", 
                "Laundromat nearby"
            ], 
            "bathroom" : [
                "Clothing storage: closet", 
                "Hair dryer", 
                "Cleaning products", 
                "Shampoo", 
                "Bathtub"
            ], 
            "bedroom" : [
                "Hangers", 
                "Iron"
            ], 
            "outdoor" : [
                "Backyard", 
                "Outdoor dining area", 
                "Lockbox", 
                "Fire pit", 
                "Free parking on premises"
            ], 
            "accessibility" : [
                "Wide entrance for guests", 
                "Step-free path to entrance", 
                "No stairs or steps to enter", 
                "Shower chair", 
                "Step-free shower"
            ]
        }, 
        "reviews_top_categories" : [
            "Cleanliness", 
            "Accuracy", 
            "Value"
        ], 
        "highlights" : [
            "Free parking", 
            "Dedicated workspace", 
            "Superhost", 
            "Entire home", 
            "Fireplace", 
            "Enhanced Clean"
        ], 
        "cancellation_policy" : "strict", 
        "listing_type" : "bed and breakfast", 
        "host_type" : "hotel", 
        "booking_requirements" : "prepayment", 
        "host_language" : "russian", 
        "location_tags" : [
            "beach", 
            "woods", 
            "desert", 
            "lake", 
            "sun", 
            "sea"
        ], 
        "eco_friendly" : false, 
        "instant_book" : true, 
        "design_tags" : [
            "dutch", 
            "greek", 
            "spanish", 
            "industrial", 
            "provincial", 
            "tree house"
        ], 
        "beds_count" : NumberInt(6), 
        "bathrooms_count" : NumberInt(2), 
        "bedrooms_count" : NumberInt(1), 
        "legal" : {
            "ownership_type" : "Community property", 
            "mortgage_property" : true
        }
    }
}]
```

</p>
</details>

### Execution
```bash
maven clean install
java -XX:StartFlightRecording=dumponexit=true -jar target/lucene-facets-benchmark-1.0.0-jar-with-dependencies.jar /home/user/config.json
```
