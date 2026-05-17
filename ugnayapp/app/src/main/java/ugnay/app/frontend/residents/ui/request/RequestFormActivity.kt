package ugnay.app.frontend.residents.ui.request

import android.os.Bundle
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
        val tvUserId = findViewById<TextView>(R.id.tv_user_id)
        val tvFullName = findViewById<TextView>(R.id.tv_full_name)
        val spinner = findViewById<Spinner>(R.id.sp_document)
        val etPurpose = findViewById<EditText>(R.id.et_form_purpose)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_request)

        btnBack.setOnClickListener {
            finish()
        }

        val user = LoginRepository.getCurrentUser()

        tvUserId.text = user?.userId ?: "Guest"
        tvFullName.text = "${user?.firstName ?: ""} ${user?.lastName ?: ""}"

        val docs = arrayOf(
            "Certificate of Indigency",
            "Certificate of Residency",
            "Barangay ID",
            "Barangay Clearance",
            "Others"
        )

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            docs
        )

        btnSubmit.setOnClickListener {

            val request = Request(
                userId = user?.userId,
                fullName = tvFullName.text.toString(),
                purpose = etPurpose.text.toString().trim()
            )

            lifecycleScope.launch {
                try {
                    btnSubmit.isEnabled = false

                    RequestRepository.submitRequest(request)

                    Toast.makeText(this@RequestFormActivity,
                        "Request Submitted!",
                        Toast.LENGTH_SHORT).show()

                    finish()

                } catch (e: Exception) {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@RequestFormActivity,
                        e.message,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}