package com.caterassist.app.activities;

import android.app.Activity;
import android.os.Bundle;

import com.caterassist.app.R;
import com.caterassist.app.adapters.FAQAdapter;
import com.caterassist.app.models.FAQ;
import com.caterassist.app.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FAQActivity extends Activity {

    private RecyclerView faqRecyclerView;
    private ArrayList<FAQ> faqArrayList;
    private FAQAdapter faqAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        initFields();
        String databasePath = FirebaseUtils.getDatabaseMainBranchName() + FirebaseUtils.FAQ_BRANCH;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                faqArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FAQ faq = snapshot.getValue(FAQ.class);
                    faqArrayList.add(faq);
                }
                faqAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initFields() {
        faqRecyclerView = findViewById(R.id.act_faq_questions_recyc_view);
        faqArrayList = new ArrayList<>();
        faqAdapter = new FAQAdapter();
        faqAdapter.setFaqArrayList(faqArrayList);
        RecyclerView.LayoutManager allVendorsLayoutManager = new LinearLayoutManager(FAQActivity.this, RecyclerView.VERTICAL, false);
        faqRecyclerView.setLayoutManager(allVendorsLayoutManager);
        faqRecyclerView.setAdapter(faqAdapter);
        faqRecyclerView.addItemDecoration(new DividerItemDecoration(FAQActivity.this, DividerItemDecoration.VERTICAL));
    }
}
