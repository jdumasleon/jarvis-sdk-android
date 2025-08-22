package com.jarvis.demo.presentation.preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.jarvis.demo.R
import com.jarvis.demo.presentation.utlis.setRoundedBgM3

class PreferencesAdapter(
    private val onPreferenceValueChanged: (String, String, PreferenceType) -> Unit,
    private val onPreferenceClick: (PreferenceItem) -> Unit
) : ListAdapter<PreferenceItem, PreferencesAdapter.PreferenceViewHolder>(PreferenceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preference, parent, false)
        return PreferenceViewHolder(view, onPreferenceValueChanged, onPreferenceClick)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        // Add bounds checking to prevent crashes
        if (position >= 0 && position < itemCount) {
            holder.bind(getItem(position))
        }
    }
    
    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty() || position < 0 || position >= itemCount) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Handle partial updates for better performance
            holder.bind(getItem(position))
        }
    }

    class PreferenceViewHolder(
        itemView: View,
        private val onPreferenceValueChanged: (String, String, PreferenceType) -> Unit,
        private val onPreferenceClick: (PreferenceItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val preferenceKey: MaterialTextView = itemView.findViewById(R.id.preference_key)
        private val preferenceType: MaterialTextView = itemView.findViewById(R.id.preference_type)
        private val typeIndicator: MaterialTextView = itemView.findViewById(R.id.type_indicator)
        private val preferenceValue: MaterialTextView = itemView.findViewById(R.id.preference_value)
        private val preferenceSwitch: SwitchCompat = itemView.findViewById(R.id.preference_switch)
        private val preferenceEditText: TextInputEditText = itemView.findViewById(R.id.preference_edit_text)
        private val editButton: MaterialButton = itemView.findViewById(R.id.edit_button)
        private val saveButton: MaterialButton = itemView.findViewById(R.id.save_button)
        private val cancelButton: MaterialButton = itemView.findViewById(R.id.cancel_button)
        private val readOnlyIndicator: MaterialTextView = itemView.findViewById(R.id.read_only_indicator)
        private val valueDisplayContainer: LinearLayout = itemView.findViewById(R.id.value_display_container)
        private val valueEditContainer: LinearLayout = itemView.findViewById(R.id.value_edit_container)

        private var currentPreference: PreferenceItem? = null
        private var isEditing = false

        fun bind(preference: PreferenceItem) {
            currentPreference = preference
            isEditing = false // Reset editing state on new bind
            
            // Set key and type
            preferenceKey.text = preference.key
            preferenceType.text = preference.type.name.lowercase()
            
            // Set type indicator
            setupTypeIndicator(preference.type)
            
            // Handle different preference types based on what can be edited in the demo
            when (preference.type) {
                PreferenceType.BOOLEAN -> {
                    // Boolean preferences from SharedPreferences can be edited
                    if (isFromSharedPreferences(preference)) {
                        setupBooleanPreference(preference)
                    } else {
                        setupReadOnlyPreference(preference)
                    }
                }
                PreferenceType.PROTO -> {
                    // Proto preferences are always read-only in demo
                    setupReadOnlyPreference(preference)
                }
                PreferenceType.STRING, PreferenceType.NUMBER -> {
                    // Only SharedPreferences STRING/NUMBER can be edited in demo
                    if (isFromSharedPreferences(preference)) {
                        setupEditablePreference(preference)
                    } else {
                        setupReadOnlyPreference(preference)
                    }
                }
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onPreferenceClick(preference)
            }
        }
        
        private fun isFromSharedPreferences(preference: PreferenceItem): Boolean {
            // In the demo, we can identify SharedPreferences by checking if they don't have certain prefixes
            // that are used by DataStore or Proto preferences
            val key = preference.key
            return !key.contains(".proto") && 
                   !key.contains("datastore") && 
                   !key.contains("_datastore") &&
                   preference.type != PreferenceType.PROTO
        }

        private fun setupTypeIndicator(type: PreferenceType) {
            val (color, text) = when (type) {
                PreferenceType.STRING -> Pair(R.color.type_string_color, "STR")
                PreferenceType.BOOLEAN -> Pair(R.color.type_boolean_color, "BOOL")
                PreferenceType.NUMBER -> Pair(R.color.type_number_color, "NUM")
                PreferenceType.PROTO -> Pair(R.color.type_proto_color, "PROTO")
            }
            
            typeIndicator.text = text
            typeIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, color))
            val rBadge = itemView.resources.getDimension(R.dimen.radius_m)
            typeIndicator.setRoundedBgM3(color, rBadge)
        }

        private fun setupBooleanPreference(preference: PreferenceItem) {
            // Hide other views and show switch
            valueDisplayContainer.visibility = View.GONE
            valueEditContainer.visibility = View.GONE
            readOnlyIndicator.visibility = View.GONE
            preferenceSwitch.visibility = View.VISIBLE
            
            // Clear any existing listener to prevent unwanted triggers
            preferenceSwitch.setOnCheckedChangeListener(null)
            
            // Set switch value
            preferenceSwitch.isChecked = preference.value.toBoolean()
            
            // Set switch listener after setting the value
            preferenceSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Only trigger if the value actually changed
                if (isChecked != preference.value.toBoolean()) {
                    onPreferenceValueChanged(preference.key, isChecked.toString(), preference.type)
                }
            }
        }

        private fun setupProtoPreference(preference: PreferenceItem) {
            setupReadOnlyPreference(preference)
        }
        
        private fun setupReadOnlyPreference(preference: PreferenceItem) {
            // Show read-only display
            valueDisplayContainer.visibility = View.VISIBLE
            valueEditContainer.visibility = View.GONE
            preferenceSwitch.visibility = View.GONE
            readOnlyIndicator.visibility = View.VISIBLE
            
            // Hide edit button for read-only preferences
            editButton.visibility = View.GONE
            
            preferenceValue.text = preference.value
        }

        private fun setupEditablePreference(preference: PreferenceItem) {
            preferenceSwitch.visibility = View.GONE
            readOnlyIndicator.visibility = View.GONE
            
            // Clear any existing edit state when binding new data
            isEditing = false
            showDisplayMode(preference)
        }

        private fun showDisplayMode(preference: PreferenceItem) {
            valueDisplayContainer.visibility = View.VISIBLE
            valueEditContainer.visibility = View.GONE
            editButton.visibility = View.VISIBLE
            
            preferenceValue.text = preference.value
            
            // Clear any existing listener before setting new one
            editButton.setOnClickListener(null)
            editButton.setOnClickListener {
                isEditing = true
                showEditMode(preference)
            }
        }

        private fun showEditMode(preference: PreferenceItem) {
            valueDisplayContainer.visibility = View.GONE
            valueEditContainer.visibility = View.VISIBLE
            
            preferenceEditText.setText(preference.value)
            preferenceEditText.requestFocus()
            
            // Set input type based on preference type
            when (preference.type) {
                PreferenceType.NUMBER -> {
                    preferenceEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
                else -> {
                    preferenceEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT
                }
            }
            
            // Clear existing listeners before setting new ones
            saveButton.setOnClickListener(null)
            cancelButton.setOnClickListener(null)
            
            saveButton.setOnClickListener {
                val newValue = preferenceEditText.text?.toString() ?: ""
                if (newValue != preference.value && newValue.isNotEmpty()) {
                    onPreferenceValueChanged(preference.key, newValue, preference.type)
                }
                isEditing = false
                showDisplayMode(preference.copy(value = newValue))
            }
            
            cancelButton.setOnClickListener {
                isEditing = false
                showDisplayMode(preference)
            }
        }
    }

    private class PreferenceDiffCallback : DiffUtil.ItemCallback<PreferenceItem>() {
        override fun areItemsTheSame(oldItem: PreferenceItem, newItem: PreferenceItem): Boolean {
            return oldItem.key == newItem.key && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: PreferenceItem, newItem: PreferenceItem): Boolean {
            return oldItem.key == newItem.key && 
                   oldItem.value == newItem.value && 
                   oldItem.type == newItem.type
        }
        
        override fun getChangePayload(oldItem: PreferenceItem, newItem: PreferenceItem): Any? {
            // Return a payload to optimize partial updates
            return if (oldItem.value != newItem.value) "VALUE_CHANGED" else null
        }
    }
}