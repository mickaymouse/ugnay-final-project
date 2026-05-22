package ugnay.app.frontend.residents.ui.request

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.request.RequestRepository

class RequestFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_form)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val etFullName = findViewById<EditText>(R.id.et_full_name)
        val tvSelectedDocument = findViewById<TextView>(R.id.tv_selected_document)
        val etPurpose = findViewById<EditText>(R.id.et_form_purpose)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_request)

        btnBack.setOnClickListener {
            finish()

        }

        // Get document type from intent
        val requestType = intent.getStringExtra("REQUEST_TYPE") ?: "General Request"
        tvSelectedDocument.text = requestType

        // Pre-fill user name but keep it editable
        val user = LoginRepository.getCurrentUser()
        if (user != null) {
            etFullName.setText("${user.firstName} ${user.lastName}")
        }

        btnSubmit.setOnClickListener {
            val purpose = etPurpose.text.toString().trim()
            val fullName = etFullName.text.toString().trim()

            val request = Request(
                fullName = fullName,
                type = requestType,
                purpose = purpose
            )

            val errors = RequestRepository.validateRequest(request)
            if (errors.isNotEmpty()) {
                if (errors.containsKey("fullName")) {
                    etFullName.error = errors["fullName"]
                }
                if (errors.containsKey("purpose")) {
                    etPurpose.error = errors["purpose"]
                }
                Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    btnSubmit.isEnabled = false
                    RequestRepository.submitRequest(request)
                    showSuccessDialog()
                } catch (e: Exception) {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@RequestFormActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_success_request)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<Button>(R.id.btn_close_dialog)
        btnClose.setOnClickListener {
            dialog.dismiss()
            finish() // Return to the previous screen
        }

        dialog.show()
    }
}
