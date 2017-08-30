package com.sensorslife.Test;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorslife.R;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


/**
 * Created by Administrator on 2017/8/25.
 */

public class CardViewTest extends AppCompatActivity {

    public int image[]={
//            R.drawable.p01,R.drawable.p02,R.drawable.p03,R.drawable.p04,
//            R.drawable.p05,R.drawable.p06,R.drawable.p07,R.drawable.p08,
//            R.drawable.p09,R.drawable.p10,R.drawable.p11,R.drawable.p12,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerviewtest);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//纵向瀑布流的实现
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(
                        2, StaggeredGridLayoutManager.VERTICAL));

        List<Map<String,Object>> memberList = new ArrayList<Map<String,Object>>();
        Map<String,Object> member=new HashMap<String,Object>();

        for (int i=0;i<12;i++)
        {
            member.put("image",image[i]);
            memberList.add(member);
        }
//        List<Member> memberList = new ArrayList<Member>();
//        memberList.add(new Member(92, R.drawable.p05, "James"));
        //      memberList.add(new Member(103, R.drawable.p06, "David"));
//        memberList.add(new Member(234, R.drawable.p09, "Jerry"));
//        memberList.add(new Member(35, R.drawable.p10, "Maggie"));
//        memberList.add(new Member(23, R.drawable.p01, "John"));
//        memberList.add(new Member(75, R.drawable.p02, "Jack"));
//        memberList.add(new Member(65, R.drawable.p03, "Mark"));
//        memberList.add(new Member(12, R.drawable.p04, "Ben"));
//        memberList.add(new Member(45, R.drawable.p07, "Ken"));
//        memberList.add(new Member(78, R.drawable.p08, "Ron"));
//        memberList.add(new Member(57, R.drawable.p11, "Sue"));
//        memberList.add(new Member(61, R.drawable.p12, "Cathy"));
        recyclerView.setAdapter(new MemberAdapter(this, memberList));
    }


/**/
private class MemberAdapter extends
                            RecyclerView.Adapter<MemberAdapter.ViewHolder>
{
        private Context context;
        private LayoutInflater layoutInflater;
        private List<Map<String,Object>> memberList;

        public MemberAdapter(Context context, List<Map<String,Object>> memberList) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
            this.memberList = memberList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName;
            View itemView;

            public ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
            }
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = layoutInflater.inflate(
                    R.layout.layout_cardview, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            //
            final Map<String,Object> member = memberList.get(position);
            Object object=null;
            for (String s:member.keySet())
                object= member.get(s);
            int obj=Integer.parseInt(String.valueOf(object));
            viewHolder.ivImage.setImageResource(obj);
            viewHolder.tvName.setText("联系人");
  /*         viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView imageView = new ImageView(context);
                    imageView.setImageResource(obj);
                    Toast toast = new Toast(context);
                    toast.setView(imageView);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
*/         }
    }
 }