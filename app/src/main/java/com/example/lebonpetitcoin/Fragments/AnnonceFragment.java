package com.example.lebonpetitcoin.Fragments;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lebonpetitcoin.Adapter.AdapterCategorie;
import com.example.lebonpetitcoin.AddAnnonceActivity;
import com.example.lebonpetitcoin.ClassFirestore.Annonce;
import com.example.lebonpetitcoin.ClassFirestore.AnnonceSignale;
import com.example.lebonpetitcoin.ClassFirestore.Categorie;
import com.example.lebonpetitcoin.ClassFirestore.Compte;
import com.example.lebonpetitcoin.ClassFirestore.Conversation;
import com.example.lebonpetitcoin.ClassFirestore.MoyenDePaiement;
import com.example.lebonpetitcoin.ClassFirestore.Position;
import com.example.lebonpetitcoin.ClassFirestore.Statistique;
import com.example.lebonpetitcoin.CustomToast;
import com.example.lebonpetitcoin.GlideApp;
import com.example.lebonpetitcoin.MainActivity;
import com.example.lebonpetitcoin.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static androidx.core.content.ContextCompat.getSystemService;


public class AnnonceFragment extends Fragment {

    TextView titre;
    TextView description,all;
    TextView moyenDePaiements,categories,nbVisites,date;

    ImageView image1;
    ImageView image2;
    ImageView image3;
    ImageView image4;
    ImageView image5;
    ImageView image6;

    String auteur;
    String lecteur = "";
    String titreAnnonce;
    String idAnnonce;

    ArrayList<ImageView> images = new ArrayList<>();

    Button contacter;
    Button signaler;
    Button profile;


    private static final String TAG = "AnnonceFragment";

    //RECUPERATION DE LA DB
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private CollectionReference cAnnonce = firestoreDB.collection("Annonce");
    private CollectionReference cCategorie = firestoreDB.collection("Categorie");
    private CollectionReference cStatistique = firestoreDB.collection("Statistique");
    private CollectionReference cConversation = firestoreDB.collection("Conversation");
    private CollectionReference cAnnonceSignale = firestoreDB.collection("AnnonceSignale");
    public FirebaseAuth mAuth;

    //Listener afin que la recherche dans la db se fasse pas quand l'application est en arrière plan
    private ListenerRegistration annonceListener;
    private ListenerRegistration categorieListener;
    private ListenerRegistration moyenDePaiementListener;
    private ChipGroup chipGroup;



    public static Fragment newInstance() {
            return (new AnnonceFragment());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_annonce, container, false);

        //Champs d'écritures
        titre= view.findViewById(R.id.titre);
        all = view.findViewById(R.id.all);
        image1 = view.findViewById(R.id.image1);
        image2 = view.findViewById(R.id.image2);
        image3 = view.findViewById(R.id.image3);
        image4 = view.findViewById(R.id.image4);
        image5 = view.findViewById(R.id.image5);
        image6 = view.findViewById(R.id.image6);


        chipGroup = (ChipGroup) view.findViewById(R.id.chipGroup);
        contacter = view.findViewById(R.id.contacter);
        signaler = view.findViewById(R.id.signaler);
        profile = view.findViewById(R.id.profile);
        description = view.findViewById(R.id.description);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        initButtons();

    }

    public void onStart() {
        super.onStart();
        //adapter.startListening();
        Bundle bundle = this.getArguments();
        final ArrayList<String>[] idCategorie = new ArrayList[]{new ArrayList<>()};
        images.add(image1);
        images.add(image2);
        images.add(image3);
        images.add(image4);
        images.add(image5);
        images.add(image6);

        String id = null;

        if (bundle != null) {
            id = bundle.getString("idAnnonce","");
        }

        if (id != null) {
            String finalId = id;
            Task<DocumentSnapshot> tAnnonce = cAnnonce.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                    Annonce annonce = documentSnapshot.toObject(Annonce.class);

                    assert annonce != null;
                    Integer nb = (annonce.getNbDeVisites() + 1);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nbDeVisites", nb);
                    if (annonce.getEstProfessionnel()==true){
                        addStat(annonce.getAuteur(),documentSnapshot.getId());
                    }

                    cAnnonce.document(finalId).update(updates);

                    titre.setText(annonce.getTitre());
                    for(int i = 0 ; i<annonce.getImages().size(); i++) {
                        images.get(i).setVisibility(View.VISIBLE);
                        GlideApp.with(getContext())
                                .load(annonce.getImages().get(i))
                                .centerCrop()
                                .into(images.get(i));


                        int finalI = i;
                        images.get(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LayoutInflater inflater = getLayoutInflater();
                                final View formElementsView = inflater.inflate(R.layout.item_image,
                                        null, false);

                                ImageView imageView = formElementsView
                                        .findViewById(R.id.imageView);
                                GlideApp.with(getContext())
                                        .load(annonce.getImages().get(finalI))
                                        .into(imageView);

                                // the alert dialog
                                new AlertDialog.Builder(getContext()).setView(formElementsView)
                                        .show();
                            }
                        });
                    }


                    if (annonce.getImages().size()==0){
                        images.get(0).setVisibility(View.VISIBLE);
                        GlideApp.with(getContext())
                                .load("https://firebasestorage.googleapis.com/v0/b/lebonpetitcoin-6928c.appspot.com/o/no_image.png?alt=media&token=e4e42748-45d3-4c07-8028-d767efda4846")
                                .into(images.get(0));
                    }
                    lecteur =  ((MainActivity)getActivity()).lecteur ;
                    if(lecteur.equals(annonce.getAuteur())==false){
                        contacter.setVisibility(View.VISIBLE);
                    }
                    if (mAuth.getCurrentUser()==null){
                        contacter.setVisibility(View.GONE);
                    }
                    description.setText(annonce.getDescription());
                    String text = getString(R.string.auteur)+ " : " + annonce.getAuteur() + "\n" +
                            getString(R.string.prix)+" : " + String.valueOf(annonce.getPrix()) +"€" +"\n" +
                            getString(R.string.date)+" : " + dateFormat.format(annonce.getDatePoste()) + "\n" ;
                    if (annonce.getDepartement()!=null && annonce.getPosition()!=null){
                        if (annonce.getDepartement().length()>0)
                        text+= getString(R.string.departement)+" : " + annonce.getDepartement()+"\n";

                        else{
                            Location location = new Location("Position Annonce");
                            location.setLatitude(annonce.getPosition().getLatitude());
                            location.setLongitude(annonce.getPosition().getLongitude());
                            String adresse = setAdresse(location);

                            if(adresse!=null){
                                text+= getString(R.string.adresse)+" : " + adresse+"\n";
                            }


                        }
                    }


                    auteur = annonce.getAuteur();
                    titreAnnonce = annonce.getTitre();
                    idAnnonce = documentSnapshot.getId();

                    text+=getString(R.string.moyen_de_paiement_s)+" : ";
                    for(String s : annonce.getPaiement())
                    {
                        text+=s+" ";
                    }
                    text+="\n";

                    text+=getString(R.string.cat_gorie_s)+" : ";
                    for(String s : annonce.getCategories())
                    {
                        text+=s+" ";
                    }
                    text+="\n";

                    all.setText(text);

                }
            });
        }

    }

    public ArrayList<String> getCategorie(ArrayList<String> categories){
        ArrayList<String> rslt = new ArrayList<>();

        if (categories.size()==0)
            return rslt;

        for (String id : categories) {
            DocumentReference docRef = cCategorie.document(id);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    rslt.add(document.getString("intitule"));
                                    //Log.d("TAG", cityName);
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                            //Toast.makeText(getContext(),rslt.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return rslt;
    }

    public void addStat(String idAuteur,String idAnnonce){
        Statistique statistique;
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            statistique= new Statistique(idAuteur,idAnnonce,false);
        }
        else
            statistique= new Statistique(idAuteur,idAnnonce,true);
        cStatistique.add(statistique)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getContext(),"erreur",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,e.toString());
                    }
                });
    }

    void contacterAuteur(String vendeur , String lecteur,String titre, String idAnnonce ,String image){
        //On verifie que l'auteur essaye pas de se contacter lui meme
        if (vendeur.equals((lecteur))==false) {
            Task<DocumentSnapshot> tConversation = cConversation.document(lecteur + idAnnonce).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                private Fragment fragmentConversation;

                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Compte compte = documentSnapshot.toObject(Compte.class);
                    String id = documentSnapshot.getId();

                    if (compte != null) {
                        if (this.fragmentConversation == null) this.fragmentConversation= ConversationFragment.newInstance();
                        Bundle arguments = new Bundle();
                        arguments.putString( "idConversation", id);
                        arguments.putString( "lecteur", lecteur);
                        fragmentConversation.setArguments(arguments);
                        ((AppCompatActivity) getContext()).getSupportFragmentManager()
                                    .beginTransaction().replace(R.id.activity_main_frame_layout, fragmentConversation).commit();
                    } else {
                        addConversation(auteur, lecteur, titre, idAnnonce, image);
                        ((MainActivity)getActivity()).showMessageFragment();
                    }
                }
            });
        }
    }

    void contacterAdmin(String vendeur , String lecteur,String titre, String idAnnonce){
        //On verifie que l'auteur essaye pas de se contacter lui meme
        if (vendeur.equals((lecteur))==false) {
            Task<DocumentSnapshot> tConversation = cConversation.document(lecteur +"Admin"+ idAnnonce).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Compte compte = documentSnapshot.toObject(Compte.class);

                    if (compte != null) {
                        //rediriger vers la conversation
                    } else {
                        addConversation("Admin", lecteur, titre, idAnnonce, "https://firebasestorage.googleapis.com/v0/b/lebonpetitcoin-6928c.appspot.com/o/red.jpg?alt=media&token=409d794c-13eb-4cad-95e4-08b8a7492a9a");
                        //rediriger vers la conversation
                    }
                }
            });
        }
    }

    void addConversation(String auteur, String lecteur, String titre , String idAnnonce, String image){
        Conversation conversation = new Conversation(auteur,lecteur,titre,idAnnonce,image);

        String id = lecteur+idAnnonce;
        if (auteur.equals("Admin")){
            id = lecteur +"Admin"+ idAnnonce;
        }

        cConversation.document(id).set(conversation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(getContext(),"Conversation crée",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getContext(),"erreur",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,e.toString());
                    }
                });
    }

    void initButtons(){
        lecteur =  ((MainActivity)getActivity()).lecteur ;
        contacter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contacterAuteur(auteur,lecteur,titreAnnonce,idAnnonce,"");
            }
        });
        /*
        signaler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contacterAdmin(auteur,lecteur,titreAnnonce,idAnnonce);
            }
        });*/

        signaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View formElementsView = inflater.inflate(R.layout.form_signaler,
                        null, false);

                final RadioGroup radioGroup = (RadioGroup) formElementsView
                        .findViewById(R.id.radioGroup);

                final EditText nameEditText = (EditText) formElementsView
                        .findViewById(R.id.messageEditText);

                // the alert dialog
                new AlertDialog.Builder(getContext()).setView(formElementsView)
                        .setTitle(R.string.alertTitreSignale)
                        .setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton(R.string.buttonSignale, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id) {
                                int selectedId = radioGroup.getCheckedRadioButtonId();

                                RadioButton selectedRadioButton = (RadioButton) formElementsView.findViewById(selectedId);

                                String raison = selectedRadioButton.getText().toString();
                                String message = nameEditText.getText().toString();

                                addAnnonceSignale(dialogInterface,idAnnonce, raison, message);
                            }
                        }).show();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            private Fragment fragmentAccount;
            @Override
            public void onClick(View v) {
                if (this.fragmentAccount == null) this.fragmentAccount=AccountFragment.newInstance();
                Bundle arguments = new Bundle();
                arguments.putString( "pseudo", auteur);
                fragmentAccount.setArguments(arguments);
                ((AppCompatActivity)getContext()).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.activity_main_frame_layout, fragmentAccount).commit();
            }
        });
    }

    private String setAdresse(Location localisation){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> adresses = null;
        try
        {
            adresses = geocoder.getFromLocation(localisation.getLatitude(), localisation.getLongitude(), 1);
        }
        catch (IOException ioException)
        {
            Log.e("GPS", "erreur", ioException);
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            Log.e("GPS", "erreur ", illegalArgumentException);
        }

        if (adresses == null || adresses.size()  == 0)
        {
            Log.e("GPS", "erreur aucune adresse !");
        }
        else
        {
            Address adresse = adresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            String strAdresse = adresse.getAddressLine(0) + ", " + adresse.getLocality();
            Log.d("GPS", "adresse : " + strAdresse);

            for(int i = 0; i <= adresse.getMaxAddressLineIndex(); i++)
            {
                addressFragments.add(adresse.getAddressLine(i));
            }
            Log.d("GPS", TextUtils.join(System.getProperty("line.separator"), addressFragments));
            return TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }
        return  null;
    }

    void addAnnonceSignale(DialogInterface dialogInterface,String idAnnonce, String raison, String message){
        AnnonceSignale annonceSignale = new AnnonceSignale(idAnnonce,raison,message);

        cAnnonceSignale.add(annonceSignale)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        dialogInterface.cancel();
                        Toast.makeText(getContext(), getString(R.string.signale),Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), getString(R.string.badSignale),Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
