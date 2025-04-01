package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
/**
 * A Pocketbase app cron job used by the CronService.
 *
 * @param expression The cron expression used to designate how often the
 *    given job is run.
 */
public data class CronJob(val expression: String) : BaseModel() {
    override fun toString(): String {
        return "CronJob(expression='$expression' id='$id')"
    }
}