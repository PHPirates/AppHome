package com.abbyberkers.apphome.communication.textformatters

/**
 * Enum class containing the templates for the languages Dutch and English.
 */
enum class Template(val dutch: (String) -> String,
                    val english: (String) -> String) {

    DEFAULT({"Ik ben rond $it thuis."}, {"I am home around $it."}),
    YAY({"Yay om $it!"}, {"Yay at $it!"}),
    ETA({"ETA $it."}, {"ETA $it."})
}