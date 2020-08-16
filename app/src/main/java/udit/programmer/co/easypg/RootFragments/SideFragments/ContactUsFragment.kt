package udit.programmer.co.easypg.RootFragments.SideFragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_contact_us.*
import udit.programmer.co.easypg.R


class ContactUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mobile_contactUs.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:9205508109")))
        }
        whatsapp_contactUs.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("smsto:9205508109")
                ).setPackage("com.whatsapp")
            )
        }
        facebook_contactUs.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/udit.jain.7796")))
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://facebook.com/udit.jain.7796")
                    )
                )
            }
        }
        instagram_contactUs.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/_u/uditjain_100")
                    ).setPackage("com.instagram.android")
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/uditjain_100")
                    )
                )
            }
        }
        twitter_contactUs.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("twitter://user?user_id=1174568369246199808")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/uditjain_100")
                    )
                )
            }
        }
        youtube_contactUs.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/channel/UCl02yB6Q9oiQjrNSn_KXzyA?view_as=subscriber")
                )
            )
        }
        email_contactUs.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:jain30udit@gmail.com")))
        }
        linkedin_contactUs.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.linkedin.com/profile/view?id=udit-jain-8a38a016a")
                )
            )
        }
    }

}