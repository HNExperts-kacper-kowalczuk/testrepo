package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.result_animal_derived_a11y
import com.hnexperts.cosmetics.resources.result_animal_derived_chip
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultAnimalDerivedChip() {
    val label: String = stringResource(Res.string.result_animal_derived_chip)
    val description: String = stringResource(Res.string.result_animal_derived_a11y)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.semantics { contentDescription = description }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
