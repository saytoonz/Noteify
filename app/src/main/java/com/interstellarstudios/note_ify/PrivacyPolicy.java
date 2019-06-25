package com.interstellarstudios.note_ify;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        int colorLightThemeText = getResources().getColor(R.color.colorLightThemeText);
        String colorLightThemeTextString = Integer.toString(colorLightThemeText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "Privacy Policy" + "</font>"));

        TextView privacyPolicy = findViewById(R.id.privacyPolicy);

        String policyText = ("This App/website is operated by Interstellar Studios. We take your privacy very seriously therefore we urge to read this policy very carefully because it contains important information about: who we are, how and why we collect, store, use and share personal information, your rights in relation to your personal information, and how to contact us and supervisory authorities in the event that you have a complaint. \n" +
                "\n" +
                "Who we are \n" +
                "\n" +
                "Interstellar Studios (we or 'us') collect, use and are responsible for certain personal information about you. When we do so we are regulated under the General Data protection Regulations which apply across the European Union (including the United Kingdom) and we are responsible as 'controller' of that personal information for the purposes of those laws. \n" +
                "\n" +
                "The personal information we collect and use \n" +
                "a) Personal information you provide to us \n" +
                "We collect the following personal information that you provide to us:\n" +
                "Name Last name Email address \n" +
                "\n" +
                "b) Personal information you provide about third parties \n" +
                "If you give us information about another person, you confirm that the other person has appointed you to act on their behalf and agreed that you: shall consent on their behalf to the processing of their personal data; shall receive any data protection notices on their behalf; and shall consent on their behalf to the transfer of their personal data abroad. \n" +
                "\n" +
                "How we use your personal information\n" +
                "\n" +
                "We collect information about our users for the following purposes: \n" +
                "Account/profile information \n" +
                "\n" +
                "Who your information may be shared with \n" +
                "\n" +
                "We may share your information with: Law enforcement agencies in connection with any investigation to help prevent unlawful activity. We will not share your personal information with any other 3rd parties.\n" +
                "\n" +
                "Whether personal information has to be provided by you, and if so why no personal information has to be provided by you to us at any time. \n" +
                "\n" +
                "How long your personal information will be kept \n" +
                "\n" +
                "We will hold your personal information until you choose to delete your account.\n" +
                "\n" +
                "Reasons we can collect and use your personal information \n" +
                "\n" +
                "We rely on the following as the lawful basis on which we collect and use your personal information: \n" +
                "\n" +
                "Keeping your information secure \n" +
                "\n" +
                "We have appropriate security measures in place to prevent personal information from being accidentally lost, or used or accessed in an unauthorised way. We limit access to your personal information to those who have a genuine business need to know it. Those processing your information will do so only in an authorised manner and are subject to a duty of confidentiality. \n" +
                "\n" +
                "We will also use technological and organisation measures to keep your information secure. These measures may include the following examples: user account access is controlled by unique name and password, information is stored on secure servers using SSL encryption.\n" +
                "We also have procedures in place to deal with any suspected data security breach. We will notify you and any applicable regulator of a suspected data security breach where we are legally required to do so. Indeed, while we will use all reasonable efforts to secure your personal data, in using our services you acknowledge that the use of the Internet is not entirely secure and for this reason we cannot guarantee the security or integrity of any personal data that are transferred from you or to you via the Internet. If you have any particular concerns about your information, please contact us using the details below. \n" +
                "\n" +
                "We will not transfer your personal information outside of the EEA at any time. \n" +
                "\n" +
                "What rights do you have? Under the General Data Protection Regulation you have a number of important rights free of charge. In summary, those include rights to: \n" +
                "    • Fair processing of information and transparency over how we use your use personal information. \n" +
                "\n" +
                "    • Access to your personal information and to certain other supplementary information that this Privacy Notice is already designed to address require us to correct any mistakes in your information which we hold \n" +
                "\n" +
                "    • Require the erasure of personal information concerning you in certain situations.\n" +
                "\n" +
                "    • Receive the personal information concerning you which you have provided to us, in a structured, commonly used and machine-readable format and have the right to transmit those data to a third party in certain situations.\n" +
                "\n" +
                "    • Object at any time to processing of personal information concerning you for direct marketing object to decisions being taken by automated means which produce legal effects concerning you or similarly significantly affect you.\n" +
                "\n" +
                "    • Object in certain other situations to our continued processing of your personal information.\n" +
                "\n" +
                "    • Otherwise restrict our processing of your personal information in certain circumstances.\n" +
                "\n" +
                "    • Claim compensation for damages caused by our breach of any data protection laws \n" +
                "For further information on each of those rights, including the circumstances in which they apply, see the Guidance from the UK Information Commissioner's Office (ICO) on individual's rights under the General Data Protection Regulations (http://ico.org.uk/for-organisations/guide-to-the-general-data-protection-regulation-gdpr/individual-rights/) If you would like to exercise any of these rights please contact us:\n" +
                "    • Let us have proof of your identity (a copy of your driving license, passport or a recent credit card/utility bill) \n" +
                "\n" +
                "    • Let us know the information to which your request relates. \n" +
                "From time to time we may also have other methods to unsubscribe (opt-out) from any direct marketing including for example, unsubscribe buttons or web links. If such are offered, please note that there may be some period after selecting to unsubscribe in which marketing may still be received while your request is being processed. \n" +
                "\n" +
                "Changes to the privacy policy \n" +
                "\n" +
                "This privacy policy was published on 09/05/2019 and last updated on 08/05/2019. We may change this privacy policy from time to time. You should check this policy occasionally to ensure you are aware of the most recent version that will apply each time you use one of our services. We will also attempt to notify users of any changes.");

        privacyPolicy.setMovementMethod(new ScrollingMovementMethod());
        privacyPolicy.setText(policyText);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(PrivacyPolicy.this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Privacy Policy" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            privacyPolicy.setTextColor(ContextCompat.getColor(PrivacyPolicy.this, R.color.colorDarkThemeText));
        }
    }
}
