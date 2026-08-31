package com.hnexperts.cosmetics.ui.ingredient

import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.function_abrasive
import com.hnexperts.cosmetics.resources.function_antimicrobial
import com.hnexperts.cosmetics.resources.function_antioxidant
import com.hnexperts.cosmetics.resources.function_chelating
import com.hnexperts.cosmetics.resources.function_cleansing
import com.hnexperts.cosmetics.resources.function_colorant
import com.hnexperts.cosmetics.resources.function_emollient
import com.hnexperts.cosmetics.resources.function_emulsifier
import com.hnexperts.cosmetics.resources.function_emulsifying
import com.hnexperts.cosmetics.resources.function_essential_oil
import com.hnexperts.cosmetics.resources.function_film_former
import com.hnexperts.cosmetics.resources.function_film_forming
import com.hnexperts.cosmetics.resources.function_foaming
import com.hnexperts.cosmetics.resources.function_fragrance
import com.hnexperts.cosmetics.resources.function_hair_conditioning
import com.hnexperts.cosmetics.resources.function_humectant
import com.hnexperts.cosmetics.resources.function_keratolytic
import com.hnexperts.cosmetics.resources.function_masking
import com.hnexperts.cosmetics.resources.function_perfuming
import com.hnexperts.cosmetics.resources.function_preservative
import com.hnexperts.cosmetics.resources.function_skin_conditioning
import com.hnexperts.cosmetics.resources.function_skin_protecting
import com.hnexperts.cosmetics.resources.function_solvent
import com.hnexperts.cosmetics.resources.function_soothing
import com.hnexperts.cosmetics.resources.function_surfactant
import com.hnexperts.cosmetics.resources.function_uv_absorber
import com.hnexperts.cosmetics.resources.function_uv_filter
import com.hnexperts.cosmetics.resources.function_viscosity_controlling
import com.hnexperts.cosmetics.resources.tag_allergen_26
import com.hnexperts.cosmetics.resources.tag_allergen_80
import com.hnexperts.cosmetics.resources.tag_animal_derived
import com.hnexperts.cosmetics.resources.tag_annex_ii
import com.hnexperts.cosmetics.resources.tag_annex_iii
import com.hnexperts.cosmetics.resources.tag_annex_iv
import com.hnexperts.cosmetics.resources.tag_annex_v
import com.hnexperts.cosmetics.resources.tag_annex_vi
import com.hnexperts.cosmetics.resources.tag_children
import com.hnexperts.cosmetics.resources.tag_cmr
import com.hnexperts.cosmetics.resources.tag_microplastic
import com.hnexperts.cosmetics.resources.tag_phototoxic
import com.hnexperts.cosmetics.resources.tag_pregnancy
import org.jetbrains.compose.resources.StringResource

object IngredientDetailTagCopy {
    fun functionLabel(tag: String): StringResource? {
        return when (tag.uppercase()) {
            "ABRASIVE" -> Res.string.function_abrasive
            "ANTIMICROBIAL" -> Res.string.function_antimicrobial
            "ANTIOXIDANT" -> Res.string.function_antioxidant
            "CHELATING" -> Res.string.function_chelating
            "CLEANSING" -> Res.string.function_cleansing
            "COLORANT" -> Res.string.function_colorant
            "EMOLLIENT" -> Res.string.function_emollient
            "EMULSIFIER" -> Res.string.function_emulsifier
            "EMULSIFYING" -> Res.string.function_emulsifying
            "ESSENTIAL_OIL" -> Res.string.function_essential_oil
            "FILM_FORMER" -> Res.string.function_film_former
            "FILM_FORMING" -> Res.string.function_film_forming
            "FOAMING" -> Res.string.function_foaming
            "FRAGRANCE" -> Res.string.function_fragrance
            "HAIR_CONDITIONING" -> Res.string.function_hair_conditioning
            "HUMECTANT" -> Res.string.function_humectant
            "KERATOLYTIC" -> Res.string.function_keratolytic
            "MASKING" -> Res.string.function_masking
            "PERFUMING" -> Res.string.function_perfuming
            "PRESERVATIVE" -> Res.string.function_preservative
            "SKIN_CONDITIONING" -> Res.string.function_skin_conditioning
            "SKIN_PROTECTING" -> Res.string.function_skin_protecting
            "SOLVENT" -> Res.string.function_solvent
            "SOOTHING" -> Res.string.function_soothing
            "SURFACTANT" -> Res.string.function_surfactant
            "UV_ABSORBER" -> Res.string.function_uv_absorber
            "UV_FILTER" -> Res.string.function_uv_filter
            "VISCOSITY_CONTROLLING" -> Res.string.function_viscosity_controlling
            else -> null
        }
    }

    fun regulatoryLabel(tag: String): StringResource? {
        return when (tag.uppercase()) {
            "ALLERGEN_26" -> Res.string.tag_allergen_26
            "ALLERGEN_80" -> Res.string.tag_allergen_80
            "ANIMAL_DERIVED" -> Res.string.tag_animal_derived
            "ANNEX_II" -> Res.string.tag_annex_ii
            "ANNEX_III" -> Res.string.tag_annex_iii
            "ANNEX_IV" -> Res.string.tag_annex_iv
            "ANNEX_V" -> Res.string.tag_annex_v
            "ANNEX_VI" -> Res.string.tag_annex_vi
            "CHILDREN" -> Res.string.tag_children
            "CMR" -> Res.string.tag_cmr
            "MICROPLASTIC" -> Res.string.tag_microplastic
            "PHOTOTOXIC" -> Res.string.tag_phototoxic
            "PREGNANCY_CAUTION" -> Res.string.tag_pregnancy
            else -> null
        }
    }
}
