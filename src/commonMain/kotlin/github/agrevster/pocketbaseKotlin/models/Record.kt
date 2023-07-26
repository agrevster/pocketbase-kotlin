package github.agrevster.pocketbaseKotlin.models

import github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
/**
 * A Pocketbase record retried from an API call
 * @property [collectionId] the ID of the record's [Collection]
 * @property [collectionName] the name of the record's [Collection]
 */
public open class Record : BaseModel() {
    public val collectionId: String? = null
    public val collectionName: String? = null
    override fun toString(): String {
        return "Record(collectionId=$collectionId, collectionName=$collectionName)"
    }
}