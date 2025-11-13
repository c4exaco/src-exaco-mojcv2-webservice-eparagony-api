package pl.exaco.receiptApi.model.external

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id

data class EForceArticle(
        @Id
        val barcode: String,
        val articleName: String,
        val articleImageUrl: String?,
        val categoryImageUrl: String?
)

data class ProductViewMixin(
        @JsonProperty("code")
        var barcode: String? = null,
        @JsonProperty("name")
        var name: String? = null,
        @JsonProperty("defaultImage")
        var defaultImage: DefaultImage? = null,
        @JsonProperty("productCategories")
        var productCategories: List<ProductCategory>? = null
)

data class DefaultImage(val name: String?)

data class ProductCategory(
        @JsonProperty("category") val category: Category? = null
)

data class Category(
        @JsonProperty("parent") val parent: Parent? = null
)

data class Parent(
        @JsonProperty("parent") val parent: ParentNested? = null
)

data class ParentNested(
        @JsonProperty("files") val files: Files? = null
)

data class Files(
        @JsonProperty("mc-icon") val mcIcon: String? = null,
        @JsonProperty("board") val board: String? = null,
        @JsonProperty("mcc-icon") val mccIcon: String? = null,
        @JsonProperty("ec4-icon") val ec4Ikon: String? = null,
        @JsonProperty("web-icon") val webIcon: String? = null
)




