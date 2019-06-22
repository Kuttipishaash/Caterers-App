package com.caterassist.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caterassist.app.R;
import com.caterassist.app.models.FAQ;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {
    private ArrayList<FAQ> faqArrayList;

    public void setFaqArrayList(ArrayList<FAQ> faqArrayList) {
        this.faqArrayList = faqArrayList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQ faq = faqArrayList.get(position);
        holder.questionTxtView.setText(faq.getQuestion());
        holder.answerTxtView.setText(faq.getAnswer());
    }

    @Override
    public int getItemCount() {
        return faqArrayList.size();
    }

    class FAQViewHolder extends RecyclerView.ViewHolder {
        private TextView questionTxtView;
        private TextView answerTxtView;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTxtView = itemView.findViewById(R.id.li_faq_question);
            answerTxtView = itemView.findViewById(R.id.li_faq_answer);
        }
    }
}
