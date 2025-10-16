package com.diary.cherry.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.diary.cherry.R
import com.diary.utils.DiarySave
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber
import java.util.UUID

class EnhancedDiaryEditorFragment : DiaryEditorFragment() {
    private lateinit var diarySave: DiarySave
    private var diaryId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_enhanced_diary_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diarySave = DiarySave(requireContext())
        diaryId = arguments?.getString("diaryId")

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveDiary()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<Button>(R.id.btnEmoji).setOnClickListener {
            openEmojiPicker()
        }

        diaryId?.let { loadDiaryContent(it) }
    }

    private fun saveDiary() {
        val title = view?.findViewById<TextInputEditText>(R.id.etTitle)?.text?.toString()
        val content = view?.findViewById<TextInputEditText>(R.id.etContent)?.text?.toString()

        if (content.isNullOrEmpty()) return

        try {
            val id = diaryId ?: UUID.randomUUID().toString()
            diarySave.saveOrUpdate(id, content, title)
            findNavController().navigateUp()
        } catch (e: Exception) {
            Timber.e(e, "Failed to save diary")
        }
    }

    private fun loadDiaryContent(id: String) {
        val content = diarySave.loadContent(id)
        val jsonString = diarySave.loadAsJsonString(id)
        val title = jsonString?.let { 
            try {
                org.json.JSONObject(it).optString("title")
            } catch (e: Exception) {
                null
            }
        }

        view?.findViewById<TextInputEditText>(R.id.etTitle)?.setText(title)
        view?.findViewById<TextInputEditText>(R.id.etContent)?.setText(content)
    }

    private fun openEmojiPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
    }
}
```

### EnhancedDiaryListFragment.kt
```kotlin
package com.diary.cherry.ui

import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.diary.utils.DiarySave
import timber.log.Timber

class EnhancedDiaryListFragment : DiaryListFragment() {
    private lateinit var diarySave: DiarySave

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diarySave = DiarySave(requireContext())
        
        setupDeleteButton()
    }

    private fun setupDeleteButton() {
        val deleteButton = view?.findViewById<Button>(R.id.btnDelete)
        deleteButton?.setOnClickListener {
            val diaryId = "current_selected_diary_id"
            deleteDiary(diaryId)
        }
    }

    private fun deleteDiary(id: String) {
        try {
            val deleted = diarySave.delete(id)
            if (deleted) {
                refreshDiaryList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete diary")
        }
    }

    private fun refreshDiaryList() {
    }
}
```

### EmojiUtils.kt
```kotlin
package com.diary.utils

import android.content.Context
import android.widget.EditText

object EmojiUtils {
    fun insertEmoji(editText: EditText, emoji: String) {
        val cursorPosition = editText.selectionStart
        val editable = editText.editableText
        
        editable.insert(cursorPosition, emoji)
    }

    fun getCommonEmojis(): List<String> {
        return listOf(
            "😊", "😂", "😍", "😢", "😠", "👍", "👎", "❤️", "🎉", "✨",
            "🌟", "🔥", "🥰", "😘", "🤔", "🙏", "👏", "👋", "🤗", "😉"
        )
    }

    fun showEmojiDialog(context: Context, onEmojiSelected: (String) -> Unit) {
        onEmojiSelected(getCommonEmojis().first())
    }
}
```

### fragment_enhanced_diary_editor.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:fillViewport="true"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:padding="16dp"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/diary_title">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etTitle"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        

        <Button
            android:id="@+id/btnPickDate"
            android:layout_marginTop="8dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/diary_pick_date" />

        <Button
            android:id="@+id/btnEmoji"
            android:layout_marginTop="8dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="插入表情" />

        <com.google.android.material.textfield.TextInputLayout
            android:layout_marginTop="8dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/diary_content">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etContent"
                android:inputType="textMultiLine"
                android:minLines="6"
                android:gravity="top|start"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        

        <LinearLayout
            android:layout_marginTop="16dp"
            android:gravity="end"
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <Button
                android:id="@+id/btnCancel"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/action_cancel" />
            <Button
                android:id="@+id/btnSave"
                android:layout_marginStart="12dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/action_save" />
        
    

```