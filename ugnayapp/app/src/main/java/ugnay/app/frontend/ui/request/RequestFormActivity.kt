package ugnay.app.frontend.ui.request

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.data.Request
import ugnay.app.backend.login.LoginRepository
import ugnay.app.backend.request.RequestRepository

class RequestFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_form)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val etName = findViewById<EditText>(R.id.et_form_name)
        val etPurpose = findViewById<EditText>(R.id.et_form_purpose)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_request)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSubmit.setOnClickListener {
            val currentUser = LoginRepository.getCurrentUser()
            val request = Request(
                userId = currentUser?.userId,
                fullName = etName.text.toString().trim(),
                purpose = etPurpose.text.toString().trim()
            )

            val errors = RequestRepository.validateRequest(request)
            if (errors.isNotEmpty()) {
                errors["fullName"]?.let { etName.error = it }
                errors["purpose"]?.let { etPurpose.error = it }
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    btnSubmit.isEnabled = false
                    RequestRepository.submitRequest(request)
                    Toast.makeText(this@RequestFormActivity, "Request Submitted Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@RequestFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
