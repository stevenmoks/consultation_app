package com.consultation.app.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.consultation.app.R;
import com.consultation.app.exception.ConsultationCallbackException;
import com.consultation.app.listener.ButtonListener;
import com.consultation.app.listener.ConsultationCallbackHandler;
import com.consultation.app.model.CaseContentTo;
import com.consultation.app.model.CaseTo;
import com.consultation.app.model.DiscussionTo;
import com.consultation.app.model.DoctorCommentsTo;
import com.consultation.app.model.ImageFilesTo;
import com.consultation.app.model.PatientCaseTo;
import com.consultation.app.model.UserTo;
import com.consultation.app.service.OpenApiService;
import com.consultation.app.util.ActivityList;
import com.consultation.app.util.BitmapCache;
import com.consultation.app.util.CaseBroadcastReceiver;
import com.consultation.app.util.ClientUtil;
import com.consultation.app.util.CommonUtil;
import com.consultation.app.util.SharePreferencesEditor;

@SuppressLint({"SimpleDateFormat", "HandlerLeak"})
public class CaseInfoNewActivity extends Activity {

    private TextView title_text, back_text, right_text;

    private LinearLayout back_layout;

    private String caseId;

    private int type;

    private SharePreferencesEditor editor;

    private RequestQueue mQueue;

    private ImageLoader mImageLoader;

    private CaseTo caseTo;

    // private ListView discussionListView;

    // private MyAdapter myAdapter=new MyAdapter();

    // private ViewHolder holder;

    private ArrayList<DiscussionTo> discussionList=new ArrayList<DiscussionTo>();

    private boolean havaCase=false, isXml=false, havaImage, isMsg;

    private TextView titleTitle, xbsTitle, jwsTitle, jzsTitle, tgjcTitle, fzjcTitle, xqTitle, bhyyTitle, bltlTitle, blpjTitle;

    private TextView infoNameText, infoSexText, infoAgeText, titleText, xbsText, jwsText, jzsText, tgjcText, xqText, bhyyText,
            blpjText, blpjNameText, blpjStarsText;

    private LinearLayout bhyyLayout, discussionLayout, showNewLayout, showDiscussionLayout, showFinshLayout, starLayout,
            examineLayout, expertDisLayout, expertDiscussionLayout, imageLayout, jyImageLayout, pjLayout, tipLayout;

    private ScrollView scrollView;

    private Button submit_btn, update_btn, evaluation_btn, discussion_send_btn, discussion_more_btn, acceptance_btn,
            not_accepted_btn, expert_send_btn, expert_submit_btn;

    private TextView evaluation_tip;

    private EditText evaluation_edit, discussion_edit, expert_dis_edit;

    private RatingBar evaluation_ratingBar, blpjRatingBar;

    private TextView bcbsText, wsjcText, zdyjText, jyssText, otherText;

    private EditText bcbsEdit, wsjcEdit, zdyjEdit, jyssEdit, otherEdit;

    private String imageString="";

    private int width;

    private int height;

    private TextView jyTextView, jcTextView;

    private Map<String, ArrayList<ImageFilesTo>> imageMap=new HashMap<String, ArrayList<ImageFilesTo>>();

//    private Map<String, String> imageTextMap=new HashMap<String, String>();

    private Map<String, ArrayList<ImageFilesTo>> jyImageMap=new HashMap<String, ArrayList<ImageFilesTo>>();

//    private Map<String, String> jyImageTextMap=new HashMap<String, String>();

    private Button tipBtn;

    private Thread thread;

    private boolean threadDisable=false, isHaveNewInfo=false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.case_info_new_layout);
        if(savedInstanceState != null) {
            ClientUtil.setToken(savedInstanceState.getString("token"));
        }
        caseId=getIntent().getStringExtra("caseId");
        type=getIntent().getIntExtra("type", -1);
        isMsg=getIntent().getBooleanExtra("isMsg", false);
        editor=new SharePreferencesEditor(CaseInfoNewActivity.this);
        ActivityList.getInstance().setActivitys("CaseInfoNewActivity", this);
        mQueue=Volley.newRequestQueue(CaseInfoNewActivity.this);
        mImageLoader=new ImageLoader(mQueue, new BitmapCache());
        WindowManager wm=this.getWindowManager();
        width=wm.getDefaultDisplay().getWidth();
        height=wm.getDefaultDisplay().getHeight();
        initData();
        initView(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("token", ClientUtil.getToken());
        super.onSaveInstanceState(outState);
    }

    private void addDiscussionView(boolean isSend, DiscussionTo AddDiscussionTo) {
        if(!isSend) {
            for(int i=0; i < discussionList.size(); i++) {
                discussionLayout.addView(getDisView(discussionList.get(i)));
            }
        } else {
            discussionLayout.addView(getDisView(AddDiscussionTo));
        }
    }

    private View getDisView(DiscussionTo discussionTo) {
        View convertView=null;
        if(discussionTo.getDiscusser_userid().equals(editor.get("uid", ""))) {
            convertView=LayoutInflater.from(CaseInfoNewActivity.this).inflate(R.layout.discussion_case_mine_list_item, null);
        } else {
            convertView=LayoutInflater.from(CaseInfoNewActivity.this).inflate(R.layout.discussion_case_list_item, null);
        }
        TextView contents=(TextView)convertView.findViewById(R.id.discussion_case_item_content);
        TextView create_time=(TextView)convertView.findViewById(R.id.discussion_case_item_createTime);
        ImageView imageView=(ImageView)convertView.findViewById(R.id.discussion_case_item_content_imageView);
        TextView name=(TextView)convertView.findViewById(R.id.discussion_case_item_name);
        TextView title=(TextView)convertView.findViewById(R.id.discussion_case_item_title);
        ImageView photo=(ImageView)convertView.findViewById(R.id.discussion_case_item_photo);
        if(discussionTo.getHave_photos().equals("1")) {
            contents.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            final String imgUrl=discussionTo.getImageFilesTos().get(0).getLittle_pic_url();
            final String bigImgUrl=discussionTo.getImageFilesTos().get(0).getPic_url();
            if(!"null".equals(imgUrl) && !"".equals(imgUrl)) {
                if(imgUrl.startsWith("http://")) {
                    imageView.setBackgroundResource(R.anim.loading_anim);
                    AnimationDrawable animation=(AnimationDrawable)imageView.getBackground();
                    animation.start();
                    imageView.setTag(imgUrl);
                    ImageListener listener=ImageLoader.getImageListener(imageView, 0, android.R.drawable.ic_menu_delete);
                    mImageLoader.get(imgUrl, listener, 200, 200);
                } else {
                    Bitmap bitmap=CommonUtil.readBitMap(200, imgUrl);
                    imageView.setImageBitmap(bitmap);
                }
                imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        BigImageActivity.setViewData(bigImgUrl);
                        startActivity(new Intent(CaseInfoNewActivity.this, BigImageActivity.class));
                    }
                });
            }
        } else {
            contents.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            contents.setText(discussionTo.getContent());
            contents.setTextSize(18);
        }
        SimpleDateFormat sdf=new SimpleDateFormat("MM-dd  HH:mm");
        String sd=sdf.format(new Date(discussionTo.getCreate_time()));
        create_time.setText(sd);
        create_time.setTextSize(15);
        title.setText("初级医师");
        title.setBackgroundResource(R.drawable.discussion_title_shape);
        if(discussionTo.getUserTo().getTp().equals("2")) {
            title.setText("专家");
            title.setBackgroundResource(R.drawable.discussion_mine_title_shape);
        }
        title.setTextSize(15);
        name.setText(discussionTo.getDiscusser());
        name.setTextSize(15);
        final String imgUrl=discussionTo.getUserTo().getIcon_url();
        photo.setTag(imgUrl);
        int photoId=0;
        if(discussionTo.getUserTo().getTp().equals("1")) {
            photoId=R.drawable.photo_primary;
        } else if(discussionTo.getUserTo().getTp().equals("2")) {
            photoId=R.drawable.photo_expert;
        }
        photo.setImageResource(photoId);
        if(!"null".equals(imgUrl) && !"".equals(imgUrl)) {
            ImageListener listener=ImageLoader.getImageListener(photo, photoId, photoId);
            mImageLoader.get(imgUrl, listener);
        }
        return convertView;
    }

    private void initData() {
        Map<String, String> parmas=new HashMap<String, String>();
        parmas.put("id", caseId);
        parmas.put("accessToken", ClientUtil.getToken());
        parmas.put("uid", editor.get("uid", ""));
        CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
        OpenApiService.getInstance(CaseInfoNewActivity.this).getPatientCaseListInfo(mQueue, parmas,
            new Response.Listener<String>() {

                @Override
                public void onResponse(String arg0) {
                    CommonUtil.closeLodingDialog();
                    try {
                        JSONObject responses=new JSONObject(arg0);
                        if(responses.getInt("rtnCode") == 1) {
                            caseTo=new CaseTo();
                            JSONObject patientCaseObject=responses.getJSONObject("patientCase");
                            PatientCaseTo patientCaseTo=new PatientCaseTo();
                            patientCaseTo.setId(patientCaseObject.getString("id"));
                            patientCaseTo.setTitle(patientCaseObject.getString("title"));
                            patientCaseTo.setProblem(patientCaseObject.getString("problem"));
                            String consult_fee=patientCaseObject.getString("consult_fee");
                            if(consult_fee.equals("null")) {
                                patientCaseTo.setConsult_fee("0");
                            } else {
                                patientCaseTo.setConsult_fee(consult_fee);
                            }
                            patientCaseTo.setConsult_tp(patientCaseObject.getString("consult_tp"));
                            patientCaseTo.setDepart_id(patientCaseObject.getString("depart_id"));
                            patientCaseTo.setStatus(patientCaseObject.getString("status"));
                            if(patientCaseObject.getString("status").equals("30")
                                || patientCaseObject.getString("status").equals("21")) {
                                caseTo.setHandleReason(responses.getString("handleReason"));
                            }
                            patientCaseTo.setStatus_desc(patientCaseObject.getString("status_desc"));
                            patientCaseTo.setPatient_name(patientCaseObject.getString("patient_name"));
                            patientCaseTo.setCase_templ_id(patientCaseObject.getString("case_templ_id"));
                            patientCaseTo.setIs_commented(patientCaseObject.getString("is_commented"));
                            if(patientCaseObject.getString("is_commented").equals("1")) {
                                DoctorCommentsTo commentsTo=new DoctorCommentsTo();
                                JSONObject jsonObject=responses.getJSONObject("expertComment");
                                commentsTo.setComment_desc(jsonObject.getString("comment_desc"));
                                commentsTo.setCommenter(jsonObject.getString("commenter"));
                                commentsTo.setStar_value(jsonObject.getInt("star_value"));
                                caseTo.setCommentsTo(commentsTo);
                            }
                            patientCaseTo.setPatient_userid(patientCaseObject.getString("patient_userid"));
                            patientCaseTo.setOpinion(patientCaseObject.getString("opinion"));
                            patientCaseTo.setDoctor_name(patientCaseObject.getString("doctor_name"));
                            patientCaseTo.setDoctor_userid(patientCaseObject.getString("doctor_userid"));
                            patientCaseTo.setExpert_name(patientCaseObject.getString("expert_name"));
                            patientCaseTo.setExpert_userid(patientCaseObject.getString("expert_userid"));
                            String createTime=patientCaseObject.getString("create_time");
                            if(createTime.equals("null")) {
                                patientCaseTo.setCreate_time("0");
                            } else {
                                patientCaseTo.setCreate_time(createTime);
                            }
                            UserTo userTo=new UserTo();
                            JSONObject userObject=patientCaseObject.getJSONObject("user");
                            userTo.setSex(userObject.getString("sex"));
                            userTo.setPhoneNumber(userObject.getString("mobile_ph"));
                            userTo.setBirth_year(userObject.getString("birth_year"));
                            userTo.setBirth_month(userObject.getString("birth_month"));
                            userTo.setBirth_day(userObject.getString("birth_day"));
                            patientCaseTo.setUserTo(userTo);
                            caseTo.setPatientCase(patientCaseTo);
                            if(!responses.getString("caseContent").equals("null")) {
                                havaCase=true;
                                CaseContentTo caseContentTo=new CaseContentTo();
                                JSONObject caseContentJsonObject=responses.getJSONObject("caseContent");
                                caseContentTo.setCaseId(caseContentJsonObject.getString("case_id"));
                                caseContentTo.setFill_tp(caseContentJsonObject.getString("fill_tp"));
                                caseContentTo.setContent_zs_xml(caseContentJsonObject.getString("content_zs_xml"));
                                if(!caseContentJsonObject.getString("content_zs_xml").equals("null")) {
                                    caseContentTo.setContent_zs_xml(caseContentJsonObject.getString("content_zs_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_zs_txt").equals("null")) {
                                    caseContentTo.setContent_zs_txt(caseContentJsonObject.getString("content_zs_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_tz_xml").equals("null")) {
                                    caseContentTo.setContent_tz_xml(caseContentJsonObject.getString("content_tz_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_tz_txt").equals("null")) {
                                    caseContentTo.setContent_tz_txt(caseContentJsonObject.getString("content_tz_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_zljg_xml").equals("null")) {
                                    caseContentTo.setContent_zljg_xml(caseContentJsonObject.getString("content_zljg_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_zljg_txt").equals("null")) {
                                    caseContentTo.setContent_zljg_txt(caseContentJsonObject.getString("content_zljg_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_jws_xml").equals("null")) {
                                    caseContentTo.setContent_jws_xml(caseContentJsonObject.getString("content_jws_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_jws_txt").equals("null")) {
                                    caseContentTo.setContent_jws_txt(caseContentJsonObject.getString("content_jws_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_jzs_xml").equals("null")) {
                                    caseContentTo.setContent_jzs_xml(caseContentJsonObject.getString("content_jzs_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_jzs_txt").equals("null")) {
                                    caseContentTo.setContent_jzs_txt(caseContentJsonObject.getString("content_jzs_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_jy_xml").equals("null")) {
                                    caseContentTo.setContent_jy_xml(caseContentJsonObject.getString("content_jy_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_jy_txt").equals("null")) {
                                    caseContentTo.setContent_jy_txt(caseContentJsonObject.getString("content_jy_txt"));
                                }
                                if(!caseContentJsonObject.getString("content_jc_xml").equals("null")) {
                                    caseContentTo.setContent_jc_xml(caseContentJsonObject.getString("content_jc_xml"));
                                }
                                if(!caseContentJsonObject.getString("content_jc_txt").equals("null")) {
                                    caseContentTo.setContent_jc_txt(caseContentJsonObject.getString("content_jc_txt"));
                                }
                                caseTo.setCaseContentTo(caseContentTo);
                            }
                            if(!responses.getString("caseFiles").equals("null")) {
                                havaImage=true;
                                imageString=responses.getString("caseFiles");
                                ArrayList<ImageFilesTo> imageFilesTos=new ArrayList<ImageFilesTo>();
                                JSONArray imageFilesArray=responses.getJSONArray("caseFiles");
                                for(int i=0; i < imageFilesArray.length(); i++) {
                                    ImageFilesTo imageFilesTo=new ImageFilesTo();
                                    JSONObject imageFilesObject=imageFilesArray.getJSONObject(i);
                                    imageFilesTo.setCase_id(imageFilesObject.getString("case_id"));
                                    imageFilesTo.setId(imageFilesObject.getString("id"));
                                    imageFilesTo.setPic_url(imageFilesObject.getString("pic_url"));
                                    imageFilesTo.setLittle_pic_url(imageFilesObject.getString("little_pic_url"));
                                    imageFilesTo.setTest_name(imageFilesObject.getString("test_name"));
                                    imageFilesTo.setItem_name(imageFilesObject.getString("case_item"));
                                    imageFilesTos.add(imageFilesTo);
                                }
                                caseTo.setImageFilesTos(imageFilesTos);
                            }
                            if(!responses.getString("caseDiscusss").equals("null")) {
                                JSONArray discussionJSONArray=responses.getJSONArray("caseDiscusss");
                                for(int i=0; i < discussionJSONArray.length(); i++) {
                                    DiscussionTo discussionTo=new DiscussionTo();
                                    JSONObject discussionObject=discussionJSONArray.getJSONObject(i);
                                    discussionTo.setId(discussionObject.getString("id"));
                                    discussionTo.setContent(discussionObject.getString("content"));
                                    String createTime1=discussionObject.getString("create_time");
                                    if(createTime1.equals("null")) {
                                        discussionTo.setCreate_time(0);
                                    } else {
                                        discussionTo.setCreate_time(Long.parseLong(createTime1));
                                    }
                                    discussionTo.setCase_id(discussionObject.getString("case_id"));
                                    discussionTo.setAt_userid(discussionObject.getString("at_userid"));
                                    discussionTo.setAt_username(discussionObject.getString("at_username"));
                                    discussionTo.setDiscusser(discussionObject.getString("discusser"));
                                    discussionTo.setDiscusser_userid(discussionObject.getString("discusser_userid"));
                                    // discussionTo.setIs_view(discussionObject.getString("is_view"));
                                    discussionTo.setHave_photos(discussionObject.getString("have_photos"));
                                    JSONObject userObject1=discussionObject.getJSONObject("user");
                                    UserTo userTo1=new UserTo();
                                    userTo1.setIcon_url(userObject1.getString("icon_url"));
                                    userTo1.setUser_name(userObject1.getString("real_name"));
                                    userTo1.setTp(userObject1.getString("tp"));
                                    discussionTo.setUserTo(userTo1);
                                    if(discussionTo.getHave_photos().equals("1")) {
                                        ImageFilesTo filesTo=new ImageFilesTo();
                                        List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                                        if(discussionObject.getString("cdFiles") != null
                                            && !"".equals(discussionObject.getString("cdFiles"))
                                            && !"null".equals(discussionObject.getString("cdFiles"))) {
                                            JSONArray jsonArray=discussionObject.getJSONArray("cdFiles");
                                            for(int j=0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject=jsonArray.getJSONObject(j);
                                                filesTo.setCase_id(jsonObject.getString("case_id"));
                                                filesTo.setPic_url(jsonObject.getString("pic_url"));
                                                filesTo.setLittle_pic_url(jsonObject.getString("little_pic_url"));
                                                filesTo.setTest_name(jsonObject.getString("test_name"));
                                                list.add(filesTo);
                                            }
                                            discussionTo.setImageFilesTos(list);
                                        }
                                    }
                                    discussionList.add(discussionTo);
                                }
                                caseTo.setDiscussionTos(discussionList);
                            }
                            handler.sendEmptyMessage(1);
                        } else if(responses.getInt("rtnCode") == 10004) {
                            Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                            LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                @Override
                                public void onSuccess(String rspContent, int statusCode) {
                                    initData();
                                }

                                @Override
                                public void onFailure(ConsultationCallbackException exp) {
                                }
                            });
                            startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError arg0) {
                    CommonUtil.closeLodingDialog();
                    Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
    }

    Handler handler=new Handler() {

        @SuppressWarnings("deprecation")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case 1:
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    });
                    // setListViewHeightBasedOnChildren(discussionListView);
                    AssetManager assetManager=getAssets();
                    try {
                        for(String str: assetManager.list("")) {
                            if(str.endsWith("case.xml")) {
                                if(str.equals(caseTo.getPatientCase().getCase_templ_id() + "case.xml")) {
                                    isXml=true;
                                }
                            }
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    infoNameText.setText(caseTo.getPatientCase().getPatient_name());
                    if(caseTo.getPatientCase().getUserTo().getSex().equals("0")) {
                        infoSexText.setText("女");
                    } else {
                        infoSexText.setText("男");
                    }
                    if(caseTo.getPatientCase().getStatus().equals("31")) {
                        // 启动请求新数据
                        if(type == 1) {
                            startInfoService();
                        }
                    }
                    Date date=new Date();
                    int currentYear=date.getYear() + 1900;
                    infoAgeText
                        .setText((currentYear - Integer.parseInt(caseTo.getPatientCase().getUserTo().getBirth_year())) + "岁");
                    titleText.setText(caseTo.getPatientCase().getTitle());
                    if(havaCase) {
                        StringBuffer xbs=new StringBuffer();
                        if(!"null".equals(caseTo.getCaseContentTo().getContent_zs_txt())
                            && !"".equals(caseTo.getCaseContentTo().getContent_zs_txt())
                            && null != caseTo.getCaseContentTo().getContent_zs_txt()) {
                            xbs.append(caseTo.getCaseContentTo().getContent_zs_txt());
                        }
                        if(!"null".equals(caseTo.getCaseContentTo().getContent_zljg_txt())
                            && !"".equals(caseTo.getCaseContentTo().getContent_zljg_txt())
                            && null != caseTo.getCaseContentTo().getContent_zljg_txt()) {
                            xbs.append(caseTo.getCaseContentTo().getContent_zljg_txt());
                        }
                        xbsText.setText(xbs.toString());
                        // zljgText.setText(caseTo.getCaseContentTo().getContent_zljg_txt());
                        jwsText.setText(caseTo.getCaseContentTo().getContent_jws_txt());
                        jzsText.setText(caseTo.getCaseContentTo().getContent_jzs_txt());
                        tgjcText.setText(caseTo.getCaseContentTo().getContent_tz_txt());
                    }
                    xqText.setText(caseTo.getPatientCase().getProblem());
                    // zxfsText.setText(caseTo.getPatientCase().getConsult_tp());
                    // ztText.setText(caseTo.getPatientCase().getStatus_desc());
                    if(caseTo.getPatientCase().getIs_commented().equals("1")) {
                        pjLayout.setVisibility(View.VISIBLE);
                        blpjTitle.setVisibility(View.VISIBLE);
                        blpjText.setText(caseTo.getCommentsTo().getComment_desc());
                        blpjNameText.setText(caseTo.getPatientCase().getExpert_name() + "获得的评价：");
                        blpjRatingBar.setRating((float)caseTo.getCommentsTo().getStar_value() / 10);
                        blpjStarsText.setText((float)caseTo.getCommentsTo().getStar_value() / 10 + "分");
                    }
                    if(havaImage) {
                        // 显示图片
                        ArrayList<ImageFilesTo> jcImageList=new ArrayList<ImageFilesTo>();
                        ArrayList<ImageFilesTo> jyImageList=new ArrayList<ImageFilesTo>();
                        for(int i=0; i < caseTo.getImageFilesTos().size(); i++) {
                            if(caseTo.getImageFilesTos().get(i).getItem_name().equals("jc")) {
                                if(!caseTo.getImageFilesTos().get(i).getTest_name().equals("其他")) {
                                    jcImageList.add(caseTo.getImageFilesTos().get(i));
                                }
                            } else if(caseTo.getImageFilesTos().get(i).getItem_name().equals("jy")) {
                                if(!caseTo.getImageFilesTos().get(i).getTest_name().equals("其他")) {
                                    jyImageList.add(caseTo.getImageFilesTos().get(i));
                                }
                            }
                        }
                        for(int i=0; i < caseTo.getImageFilesTos().size(); i++) {
                            if(caseTo.getImageFilesTos().get(i).getItem_name().equals("jc")) {
                                if(caseTo.getImageFilesTos().get(i).getTest_name().equals("其他")) {
                                    jcImageList.add(caseTo.getImageFilesTos().get(i));
                                }
                            } else if(caseTo.getImageFilesTos().get(i).getItem_name().equals("jy")) {
                                if(caseTo.getImageFilesTos().get(i).getTest_name().equals("其他")) {
                                    jyImageList.add(caseTo.getImageFilesTos().get(i));
                                }
                            }
                        }

                        if(isXml) {
                            // 显示 标题加图片
                            showJCListImageLayout(jcImageList);
                        } else {
                            // 显示图片
                            showJCImageLayout(jcImageList);
                        }
                        if(isXml) {
                            // 显示 标题加图片
                            showJYListImageLayout(jyImageList);
                        } else {
                            // 显示图片
                            showJYImageLayout(jyImageList);
                        }
                    }
                    if(caseTo.getPatientCase().getStatus().equals("21") || caseTo.getPatientCase().getStatus().equals("30")) {
                        bhyyTitle.setVisibility(View.VISIBLE);
                        bhyyLayout.setVisibility(View.VISIBLE);
                        bhyyText.setVisibility(View.VISIBLE);
                        bhyyText.setText(caseTo.getHandleReason());
                    }
                    if(caseTo.getPatientCase().getStatus().equals("13") || caseTo.getPatientCase().getStatus().equals("31")
                        || caseTo.getPatientCase().getStatus().equals("12") || caseTo.getPatientCase().getStatus().equals("40")) {
                        bltlTitle.setVisibility(View.VISIBLE);
                        // discussionListView.setVisibility(View.VISIBLE);
                        discussionLayout.setVisibility(View.VISIBLE);
                    }
                    if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                        if(editor.get("userType", "").equals("1")) {
                            if(caseTo.getPatientCase().getStatus().equals("10") || caseTo.getPatientCase().getStatus().equals("21")
                                || caseTo.getPatientCase().getStatus().equals("30")) {
                                showNewLayout.setVisibility(View.VISIBLE);
                                showDiscussionLayout.setVisibility(View.GONE);
                                showFinshLayout.setVisibility(View.GONE);
                            } else if(caseTo.getPatientCase().getStatus().equals("31")) {
                                showNewLayout.setVisibility(View.GONE);
                                showDiscussionLayout.setVisibility(View.VISIBLE);
                                showFinshLayout.setVisibility(View.GONE);
                            } else if((caseTo.getPatientCase().getStatus().equals("12") || caseTo.getPatientCase().getStatus()
                                .equals("13"))
                                && caseTo.getPatientCase().getIs_commented().equals("0")) {
                                showNewLayout.setVisibility(View.GONE);
                                showDiscussionLayout.setVisibility(View.GONE);
                                showFinshLayout.setVisibility(View.VISIBLE);
                            }
                        } else if(editor.get("userType", "").equals("2")) {
                            if(caseTo.getPatientCase().getStatus().equals("20")) {
                                examineLayout.setVisibility(View.VISIBLE);
                                expertDisLayout.setVisibility(View.GONE);
                            } else if(caseTo.getPatientCase().getStatus().equals("31")) {
                                examineLayout.setVisibility(View.GONE);
                                expertDisLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if(caseTo.getPatientCase().getConsult_tp().equals("公开讨论")) {
                        if(editor.get("userType", "").equals("1")) {
                            if(caseTo.getPatientCase().getStatus().equals("10") || caseTo.getPatientCase().getStatus().equals("21")
                                || caseTo.getPatientCase().getStatus().equals("30")) {
                                showNewLayout.setVisibility(View.VISIBLE);
                                showDiscussionLayout.setVisibility(View.GONE);
                                showFinshLayout.setVisibility(View.GONE);
                            } else if(caseTo.getPatientCase().getStatus().equals("31")) {
                                showNewLayout.setVisibility(View.GONE);
                                showDiscussionLayout.setVisibility(View.VISIBLE);
                                showFinshLayout.setVisibility(View.GONE);
                            }
                        } else if(editor.get("userType", "").equals("2")) {
                            if(caseTo.getPatientCase().getStatus().equals("31")) {
                                // 专家讨论的界面
                                expertDiscussionLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    addDiscussionView(false, null);
                    break;
                case 2:
                    tipLayout.setVisibility(View.VISIBLE);
                    isHaveNewInfo=true;
                    if(tipBtn.getText().toString() != null && !"".equals(tipBtn.getText().toString())) {
                        int cuunt=Integer.parseInt(tipBtn.getText().toString()) + msg.arg1;
                        tipBtn.setText(cuunt + "");
                    } else {
                        tipBtn.setText(msg.arg1 + "");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void startInfoService() {
        thread=new Thread(new Runnable() {

            @Override
            public void run() {
                while(!threadDisable) {
                    try {
                        Thread.sleep(30 * 1000);
                        Map<String, String> parmas=new HashMap<String, String>();
                        parmas.put("showedCount", discussionList.size() + "");
                        parmas.put("caseId", caseId);
                        parmas.put("accessToken", ClientUtil.getToken());
                        parmas.put("uid", editor.get("uid", ""));
                        OpenApiService.getInstance(CaseInfoNewActivity.this).getLastedDiscuss(mQueue, parmas,
                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String arg0) {
                                    try {
                                        JSONObject responses=new JSONObject(arg0);
                                        if(responses.getInt("rtnCode") == 1) {
                                            JSONArray infos=responses.getJSONArray("caseDiscusss");
                                            for(int i=0; i < infos.length(); i++) {
                                                JSONObject info=infos.getJSONObject(i);
                                                DiscussionTo discussionTo=new DiscussionTo();
                                                discussionTo.setId(info.getString("id"));
                                                discussionTo.setContent(info.getString("content"));
                                                String createTime=info.getString("create_time");
                                                if(createTime.equals("null")) {
                                                    discussionTo.setCreate_time(0);
                                                } else {
                                                    discussionTo.setCreate_time(Long.parseLong(createTime));
                                                }
                                                discussionTo.setCase_id(info.getString("case_id"));
                                                discussionTo.setAt_userid(info.getString("at_userid"));
                                                discussionTo.setAt_username(info.getString("at_username"));
                                                discussionTo.setDiscusser(info.getString("discusser"));
                                                discussionTo.setDiscusser_userid(info.getString("discusser_userid"));
                                                // discussionTo.setIs_view(info.getString("is_view"));
                                                discussionTo.setHave_photos(info.getString("have_photos"));
                                                JSONObject userObject=info.getJSONObject("user");
                                                UserTo userTo=new UserTo();
                                                userTo.setIcon_url(userObject.getString("icon_url"));
                                                userTo.setUser_name(userObject.getString("real_name"));
                                                userTo.setTp(userObject.getString("tp"));
                                                discussionTo.setUserTo(userTo);
                                                if(discussionTo.getHave_photos().equals("1")) {
                                                    ImageFilesTo filesTo=new ImageFilesTo();
                                                    List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                                                    if(info.getString("cdFiles") != null && !"".equals(info.getString("cdFiles"))
                                                        && !"null".equals(info.getString("cdFiles"))) {
                                                        JSONArray jsonArray=info.getJSONArray("cdFiles");
                                                        for(int j=0; j < jsonArray.length(); j++) {
                                                            JSONObject jsonObject=jsonArray.getJSONObject(j);
                                                            filesTo.setCase_id(jsonObject.getString("case_id"));
                                                            filesTo.setPic_url(jsonObject.getString("pic_url"));
                                                            filesTo.setTest_name(jsonObject.getString("test_name"));
                                                            list.add(filesTo);
                                                        }
                                                        discussionTo.setImageFilesTos(list);
                                                    }
                                                }
                                                discussionList.add(discussionTo);
                                            }
                                            if(infos.length() != 0) {
                                                Message message=new Message();
                                                message.arg1=infos.length();
                                                message.what=2;
                                                handler.sendMessage(message);
                                            }
                                        } else {
                                            Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"),
                                                Toast.LENGTH_SHORT).show();
                                        }
                                    } catch(JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError arg0) {
                                    Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void showJCListImageLayout(ArrayList<ImageFilesTo> imageFilesTos) {
        ArrayList<String> TestNames=new ArrayList<String>();
        for(int i=0; i < imageFilesTos.size(); i++) {
            String testName=imageFilesTos.get(i).getTest_name();
            if(!TestNames.contains(testName)){
                TestNames.add(testName);
            }
            if(imageMap.get(testName) == null) {
                ArrayList<ImageFilesTo> temp=new ArrayList<ImageFilesTo>();
                temp.add(imageFilesTos.get(i));
                imageMap.put(testName, temp);
            } else {
                imageMap.get(testName).add(imageFilesTos.get(i));
            }
        }
//        if(havaCase) {
//            if(null != caseTo.getCaseContentTo().getContent_jc_txt() && !"".equals(caseTo.getCaseContentTo().getContent_jc_txt())
//                && !"null".equals(caseTo.getCaseContentTo().getContent_jc_txt())) {
//                try {
//                    JSONObject imageTextObject=new JSONObject(caseTo.getCaseContentTo().getContent_jc_txt());
//                    for(int i=0; i < TestNames.size(); i++) {
//                        if(imageTextObject.toString().contains(TestNames.get(i))) {
//                            if(!TestNames.get(i).equals("其他")) {
//                                imageTextMap.put(TestNames.get(i), imageTextObject.getString(TestNames.get(i)));
//                            }
//                        }
//                    }
//                    for(int i=0; i < TestNames.size(); i++) {
//                        if(imageTextObject.toString().contains(TestNames.get(i))) {
//                            if(TestNames.get(i).equals("其他")) {
//                                imageTextMap.put(TestNames.get(i), imageTextObject.getString(TestNames.get(i)));
//                            }
//                        }
//                    }
//                } catch(JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        imageLayout.removeAllViews();
//        Iterator iter=imageMap.entrySet().iterator();
        for(int k=0; k < TestNames.size(); k++) {
            String name=TestNames.get(k);
            ArrayList<ImageFilesTo> filesTos=imageMap.get(name);
            // 先创建一个标题
            imageLayout.addView(createTextView(name, 17, "#000000"));
//            if(imageTextMap.size() != 0) {
//                if(imageTextMap.get(name) != null && !"".equals(imageTextMap.get(name))) {
//                    imageLayout.addView(createTextView(imageTextMap.get(name), 17, "#7B7B7B"));
//                }
//            }
            // 再显示图片
            if(null != filesTos && filesTos.size() != 0) {
                LinearLayout rowsLayout=new LinearLayout(CaseInfoNewActivity.this);
                LinearLayout relativeLayout=new LinearLayout(CaseInfoNewActivity.this);
                for(int i=0; i < filesTos.size(); i++) {
                    if(i % 3 == 0) {
                        rowsLayout=createLinearLayout();
                        imageLayout.addView(rowsLayout);
                    }
                    relativeLayout=createImage(filesTos.get(i).getLittle_pic_url(), filesTos.get(i).getPic_url());
                    rowsLayout.addView(relativeLayout);
                }
            }
        }
    }

    private void showJYListImageLayout(ArrayList<ImageFilesTo> imageFilesTos) {
        ArrayList<String> TestNames=new ArrayList<String>();
        for(int i=0; i < imageFilesTos.size(); i++) {
            String testName=imageFilesTos.get(i).getTest_name();
            if(!TestNames.contains(testName)){
                TestNames.add(testName);
            }
            if(jyImageMap.get(testName) == null) {
                ArrayList<ImageFilesTo> temp=new ArrayList<ImageFilesTo>();
                temp.add(imageFilesTos.get(i));
                jyImageMap.put(testName, temp);
            } else {
                jyImageMap.get(testName).add(imageFilesTos.get(i));
            }
        }
//        if(havaCase) {
//            if(null != caseTo.getCaseContentTo().getContent_jc_txt() && !"".equals(caseTo.getCaseContentTo().getContent_jc_txt())
//                && !"null".equals(caseTo.getCaseContentTo().getContent_jc_txt())) {
//                try {
//                    JSONObject imageTextObject=new JSONObject(caseTo.getCaseContentTo().getContent_jc_txt());
//                    for(int i=0; i < TestNames.size(); i++) {
//                        if(imageTextObject.toString().contains(TestNames.get(i))) {
//                            if(!TestNames.get(i).equals("其他")) {
//                                jyImageTextMap.put(TestNames.get(i), imageTextObject.getString(TestNames.get(i)));
//                            }
//                        }
//                    }
//                    for(int i=0; i < TestNames.size(); i++) {
//                        if(imageTextObject.toString().contains(TestNames.get(i))) {
//                            if(TestNames.get(i).equals("其他")) {
//                                jyImageTextMap.put(TestNames.get(i), imageTextObject.getString(TestNames.get(i)));
//                            }
//                        }
//                    }
//                } catch(JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        jyImageLayout.removeAllViews();
//        Iterator iter=jyImageMap.entrySet().iterator();
//        while(iter.hasNext()) {
        for(int k=0; k < TestNames.size(); k++) {
//            Map.Entry entry=(Map.Entry)iter.next();
            String name=TestNames.get(k);
            ArrayList<ImageFilesTo> filesTos=jyImageMap.get(name);
            // 先创建一个标题
            jyImageLayout.addView(createTextView(name, 17, "#000000"));
//            if(jyImageTextMap.size() != 0) {
//                if(jyImageTextMap.get(name) != null && !"".equals(jyImageTextMap.get(name))) {
//                    jyImageLayout.addView(createTextView(jyImageTextMap.get(name), 17, "#7B7B7B"));
//                }
//            }
            // 再显示图片
            if(null != filesTos && filesTos.size() != 0) {
                LinearLayout rowsLayout=new LinearLayout(CaseInfoNewActivity.this);
                LinearLayout relativeLayout=new LinearLayout(CaseInfoNewActivity.this);
                for(int i=0; i < filesTos.size(); i++) {
                    if(i % 3 == 0) {
                        rowsLayout=createLinearLayout();
                        jyImageLayout.addView(rowsLayout);
                    }
                    relativeLayout=createImage(filesTos.get(i).getLittle_pic_url(), filesTos.get(i).getPic_url());
                    rowsLayout.addView(relativeLayout);
                }
            }
        }
    }

    private LinearLayout createTextView(String name, int textSize, String textColor) {
        LinearLayout layout=new LinearLayout(CaseInfoNewActivity.this);
        LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);

        TextView textName=new TextView(CaseInfoNewActivity.this);
        LayoutParams textNameParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textNameParams.leftMargin=width / 50;
        textName.setLayoutParams(textNameParams);
        textName.setText(name);
        textName.setGravity(Gravity.CENTER_VERTICAL);
        textName.setTextColor(Color.parseColor(textColor));
        textName.setTextSize(textSize);
        layout.addView(textName);
        return layout;
    }

    private void showJCImageLayout(ArrayList<ImageFilesTo> imageFilesTos) {
        imageLayout.removeAllViews();
        if(null != imageFilesTos && imageFilesTos.size() != 0) {
            LinearLayout rowsLayout=new LinearLayout(CaseInfoNewActivity.this);
            LinearLayout relativeLayout=new LinearLayout(CaseInfoNewActivity.this);
            for(int i=0; i < imageFilesTos.size(); i++) {
                if(i % 3 == 0) {
                    rowsLayout=createLinearLayout();
                    imageLayout.addView(rowsLayout);
                }
                relativeLayout=createImage(imageFilesTos.get(i).getLittle_pic_url(), imageFilesTos.get(i).getPic_url());
                rowsLayout.addView(relativeLayout);
            }
        }
    }

    private void showJYImageLayout(ArrayList<ImageFilesTo> imageFilesTos) {
        jyImageLayout.removeAllViews();
        if(null != imageFilesTos && imageFilesTos.size() != 0) {
            LinearLayout rowsLayout=new LinearLayout(CaseInfoNewActivity.this);
            LinearLayout relativeLayout=new LinearLayout(CaseInfoNewActivity.this);
            for(int i=0; i < imageFilesTos.size(); i++) {
                if(i % 3 == 0) {
                    rowsLayout=createLinearLayout();
                    jyImageLayout.addView(rowsLayout);
                }
                relativeLayout=createImage(imageFilesTos.get(i).getLittle_pic_url(), imageFilesTos.get(i).getPic_url());
                rowsLayout.addView(relativeLayout);
            }
        }
    }

    private LinearLayout createLinearLayout() {
        LinearLayout linearLayout=new LinearLayout(CaseInfoNewActivity.this);
        LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity=Gravity.CENTER_VERTICAL;
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(0, height / 100, 0, height / 100);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    private LinearLayout createImage(String path, final String bigPath) {
        LinearLayout relativeLayout=new LinearLayout(CaseInfoNewActivity.this);
        LayoutParams layoutParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity=Gravity.CENTER;
        layoutParams.leftMargin=width / 55;
        layoutParams.rightMargin=width / 55;
        relativeLayout.setLayoutParams(layoutParams);
        ImageView imageView=new ImageView(CaseInfoNewActivity.this);
        LayoutParams imageViewParams=new LayoutParams(width / 15 * 4, width / 15 * 4);
        imageView.setLayoutParams(imageViewParams);
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 展示大图片
                BigImageActivity.setViewData(bigPath);
                startActivity(new Intent(CaseInfoNewActivity.this, BigImageActivity.class));
            }
        });
        imageView.setScaleType(ScaleType.CENTER_CROP);
        imageView.setTag(path);
        imageView.setBackgroundResource(R.anim.loading_anim);
        AnimationDrawable animation=(AnimationDrawable)imageView.getBackground();
        animation.start();
        if(!"null".equals(path) && !"".equals(path)) {
            ImageListener listener=ImageLoader.getImageListener(imageView, 0, android.R.drawable.ic_menu_delete);
            mImageLoader.get(path, listener, 200, 200);
        }
        relativeLayout.addView(imageView);
        return relativeLayout;
    }

    private void initView(Bundle savedInstanceState) {
        title_text=(TextView)findViewById(R.id.header_text);
        title_text.setText("病例摘要");
        title_text.setTextSize(20);
        // title_text.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // if(isHaveNewInfo) {
        // tipBtn.setVisibility(View.INVISIBLE);
        // tipBtn.setText("");
        // discussionLayout.removeAllViews();
        // addDiscussionView(false, null);
        // // myAdapter.notifyDataSetChanged();
        // // setListViewHeightBasedOnChildren(discussionListView);
        // new Handler().post(new Runnable() {
        //
        // @Override
        // public void run() {
        // scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        // }
        // });
        // isHaveNewInfo=false;
        // }
        // }
        // });

        back_layout=(LinearLayout)findViewById(R.id.header_layout_lift);
        back_layout.setVisibility(View.VISIBLE);
        back_text=(TextView)findViewById(R.id.header_text_lift);
        back_text.setTextSize(18);
        back_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                threadDisable=true;
                if(isMsg) {
                    Intent intent=new Intent(CaseInfoNewActivity.this, HomeActivity.class);
                    intent.putExtra("selectId", 0);
                    startActivity(intent);
                }
                finish();
            }
        });

        right_text=(TextView)findViewById(R.id.header_right);
        right_text.setText("复用");
        right_text.setTextSize(18);
        if(editor.get("userType", "").equals("1")) {
            right_text.setVisibility(View.VISIBLE);
        }
        right_text.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 病例复用
                Intent intent=new Intent(CaseInfoNewActivity.this, ChangeExpertActivity.class);
                intent.putExtra("caseId", caseId);
                intent.putExtra("consult_tp", caseTo.getPatientCase().getConsult_tp());
                intent.putExtra("mobile_ph", caseTo.getPatientCase().getUserTo().getPhoneNumber());
                startActivityForResult(intent, 3);
            }
        });

        blpjNameText=(TextView)findViewById(R.id.case_info_new_pj_text_expert_name);
        blpjNameText.setTextSize(16);

        blpjStarsText=(TextView)findViewById(R.id.case_info_new_pj_stars_text);
        blpjStarsText.setTextSize(18);

        blpjRatingBar=(RatingBar)findViewById(R.id.case_info_new_pj_ratingBar);

        tipLayout=(LinearLayout)findViewById(R.id.header_mid_tip_layout);
        tipBtn=(Button)findViewById(R.id.header_mid_tip);
        tipBtn.setTextSize(12);
        tipBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isHaveNewInfo) {
                    tipLayout.setVisibility(View.INVISIBLE);
                    tipBtn.setText("");
                    discussionLayout.removeAllViews();
                    addDiscussionView(false, null);
                    // myAdapter.notifyDataSetChanged();
                    // setListViewHeightBasedOnChildren(discussionListView);
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                    isHaveNewInfo=false;
                }
            }
        });

        scrollView=(ScrollView)findViewById(R.id.case_info_all_scrollView);

        bhyyLayout=(LinearLayout)findViewById(R.id.case_info_new_bhyy_layout);
        discussionLayout=(LinearLayout)findViewById(R.id.case_info_new_discussion_layout);
        showNewLayout=(LinearLayout)findViewById(R.id.case_info_new_show_new_layout);
        showDiscussionLayout=(LinearLayout)findViewById(R.id.case_info_new_show_discussion_layout);
        showFinshLayout=(LinearLayout)findViewById(R.id.case_info_new_show_finsh_layout);
        starLayout=(LinearLayout)findViewById(R.id.case_info_new_evaluation_stars_layout);
        examineLayout=(LinearLayout)findViewById(R.id.case_info_new_show_examine_layout);
        expertDiscussionLayout=(LinearLayout)findViewById(R.id.case_info_new_show_expert_discussion_layout);
        expertDisLayout=(LinearLayout)findViewById(R.id.case_info_new_show_expert_dis_layout);
        imageLayout=(LinearLayout)findViewById(R.id.case_info_new_title_fzjc_layout);
        jyImageLayout=(LinearLayout)findViewById(R.id.case_info_new_title_jy_layout);
        pjLayout=(LinearLayout)findViewById(R.id.case_info_new_pj_layout);

        jyTextView=(TextView)findViewById(R.id.case_info_new_jy_text);
        jyTextView.setTextSize(17);
        jcTextView=(TextView)findViewById(R.id.case_info_new_jc_text);
        jcTextView.setTextSize(17);

        titleTitle=(TextView)findViewById(R.id.case_info_new_title_zs_text);
        titleTitle.setTextSize(18);

        xbsTitle=(TextView)findViewById(R.id.case_info_new_title_xbs_text);
        xbsTitle.setTextSize(18);

        // zljgTitle=(TextView)findViewById(R.id.case_info_new_title_zljg_text);
        // zljgTitle.setTextSize(18);

        jwsTitle=(TextView)findViewById(R.id.case_info_new_title_jws_text);
        jwsTitle.setTextSize(18);

        jzsTitle=(TextView)findViewById(R.id.case_info_new_title_jzs_text);
        jzsTitle.setTextSize(18);

        tgjcTitle=(TextView)findViewById(R.id.case_info_new_title_tgjy_text);
        tgjcTitle.setTextSize(18);

        fzjcTitle=(TextView)findViewById(R.id.case_info_new_title_fzjc_text);
        fzjcTitle.setTextSize(18);

        xqTitle=(TextView)findViewById(R.id.case_info_new_title_xq_text);
        xqTitle.setTextSize(18);

        // zxfsTitle=(TextView)findViewById(R.id.case_info_new_title_zxfs_text);
        // zxfsTitle.setTextSize(18);

        // ztTitle=(TextView)findViewById(R.id.case_info_new_title_zt_text);
        // ztTitle.setTextSize(18);

        bhyyTitle=(TextView)findViewById(R.id.case_info_new_title_bhyy_text);
        bhyyTitle.setTextSize(18);

        bltlTitle=(TextView)findViewById(R.id.case_info_new_title_tl_text);
        bltlTitle.setTextSize(18);

        blpjTitle=(TextView)findViewById(R.id.case_info_new_title_pj_text);
        blpjTitle.setTextSize(18);

        infoNameText=(TextView)findViewById(R.id.case_info_new_patient_name_text);
        infoNameText.setTextSize(17);

        infoSexText=(TextView)findViewById(R.id.case_info_new_patient_sex_text);
        infoSexText.setTextSize(17);

        infoAgeText=(TextView)findViewById(R.id.case_info_new_patient_age_text);
        infoAgeText.setTextSize(17);

        titleText=(TextView)findViewById(R.id.case_info_new_zs_text);
        titleText.setTextSize(17);

        xbsText=(TextView)findViewById(R.id.case_info_new_xbs_text);
        xbsText.setTextSize(17);

        // zljgText=(TextView)findViewById(R.id.case_info_new_zljg_text);
        // zljgText.setTextSize(17);

        jwsText=(TextView)findViewById(R.id.case_info_new_jws_text);
        jwsText.setTextSize(17);

        jzsText=(TextView)findViewById(R.id.case_info_new_jzs_text);
        jzsText.setTextSize(17);

        tgjcText=(TextView)findViewById(R.id.case_info_new_tgjy_text);
        tgjcText.setTextSize(17);

        xqText=(TextView)findViewById(R.id.case_info_new_xq_text);
        xqText.setTextSize(17);

        // zxfsText=(TextView)findViewById(R.id.case_info_new_zxfs_text);
        // zxfsText.setTextSize(17);

        // ztText=(TextView)findViewById(R.id.case_info_new_zt_text);
        // ztText.setTextSize(17);

        bhyyText=(TextView)findViewById(R.id.case_info_new_bhyy_text);
        bhyyText.setTextSize(17);

        blpjText=(TextView)findViewById(R.id.case_info_new_pj_text);
        blpjText.setTextSize(17);

        // expertTitle=(TextView)findViewById(R.id.case_info_new_title_zjtlk_text);
        // expertTitle.setTextSize(18);
        bcbsText=(TextView)findViewById(R.id.case_info_new_title_zjtlk_bcbs_text);
        bcbsText.setTextSize(18);
        wsjcText=(TextView)findViewById(R.id.case_info_new_title_zjtlk_wsjc_text);
        wsjcText.setTextSize(17);
        zdyjText=(TextView)findViewById(R.id.case_info_new_title_zjtlk_zdyj_text);
        zdyjText.setTextSize(17);
        jyssText=(TextView)findViewById(R.id.case_info_new_title_zjtlk_jyss_text);
        jyssText.setTextSize(17);
        otherText=(TextView)findViewById(R.id.case_info_new_title_zjtlk_other_text);
        otherText.setTextSize(17);

        bcbsEdit=(EditText)findViewById(R.id.case_info_new_title_zjtlk_bcbs_input_edit);
        bcbsEdit.setTextSize(17);
        wsjcEdit=(EditText)findViewById(R.id.case_info_new_title_zjtlk_wsjc_input_edit);
        wsjcEdit.setTextSize(17);
        zdyjEdit=(EditText)findViewById(R.id.case_info_new_title_zjtlk_zdyj_input_edit);
        zdyjEdit.setTextSize(17);
        jyssEdit=(EditText)findViewById(R.id.case_info_new_title_zjtlk_jyss_input_edit);
        jyssEdit.setTextSize(17);
        otherEdit=(EditText)findViewById(R.id.case_info_new_title_zjtlk_other_input_edit);
        otherEdit.setTextSize(17);

        // discussionListView=(ListView)findViewById(R.id.case_info_new_discussion_listView);
        // discussionListView.setAdapter(myAdapter);

        submit_btn=(Button)findViewById(R.id.case_info_new_btn_submit);
        submit_btn.setTextSize(18);
        submit_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 提交申请
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("id", caseId);
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getCaseSubmitInfo(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    Toast.makeText(CaseInfoNewActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent("com.consultation.app.new.case.action");
                                    if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                                        intent.putExtra("isOpen", 1);
                                    } else {
                                        intent.putExtra("isOpen", 0);
                                    }
                                    sendBroadcast(intent);
                                    finish();
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        submit_btn.setOnTouchListener(new ButtonListener().setImage(getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());

        update_btn=(Button)findViewById(R.id.case_info_new_btn_update);
        update_btn.setTextSize(18);
        update_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 修改病例
                Intent intent=new Intent(CaseInfoNewActivity.this, CreateCaseActivity.class);
                intent.putExtra("caseId", caseId);
                intent.putExtra("departmentId", caseTo.getPatientCase().getCase_templ_id());
                intent.putExtra("expertId", caseTo.getPatientCase().getExpert_userid());
                intent.putExtra("expertName", caseTo.getPatientCase().getExpert_name());
                intent.putExtra("patientId", caseTo.getPatientCase().getPatient_userid());
                intent.putExtra("patientName", caseTo.getPatientCase().getPatient_name());
                intent.putExtra("consultType", caseTo.getPatientCase().getConsult_tp());
                intent.putExtra("titles", caseTo.getPatientCase().getTitle());
                intent.putExtra("problem", caseTo.getPatientCase().getProblem());
                intent.putExtra("imageString", imageString);
                intent.putExtra("isUpdate", true);
                StringBuffer buffer=new StringBuffer();
                if(havaCase) {
                    if(caseTo.getCaseContentTo().getFill_tp().equals("1")) {
                        // txt
                        buffer.append(caseTo.getCaseContentTo().getContent_zs_txt()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_tz_txt()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_zljg_txt()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_jws_txt()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_jzs_txt());
                        intent.putExtra("content", buffer.toString());
                    } else if(caseTo.getCaseContentTo().getFill_tp().equals("2")) {
                        // xml
                        buffer.append(caseTo.getCaseContentTo().getContent_zs_xml()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_tz_xml()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_zljg_xml()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_jws_xml()).append("&");
                        buffer.append(caseTo.getCaseContentTo().getContent_jzs_xml());
                        intent.putExtra("content", buffer.toString());
                    }
                }
                startActivity(intent);
                CaseBroadcastReceiver.setHander(new ConsultationCallbackHandler() {

                    @Override
                    public void onSuccess(String rspContent, int statusCode) {
                        initData();
                        initView(null);
                    }

                    @Override
                    public void onFailure(ConsultationCallbackException exp) {
                    }
                });
            }
        });
        update_btn.setOnTouchListener(new ButtonListener().setImage(getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());

        evaluation_btn=(Button)findViewById(R.id.case_info_new_evaluation_btn);
        evaluation_btn.setTextSize(17);
        evaluation_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 初级医师评价
                if(null == evaluation_edit.getText().toString() || "".equals(evaluation_edit.getText().toString())) {
                    Toast.makeText(CaseInfoNewActivity.this, "请输入评价内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                if((int)evaluation_ratingBar.getRating() == 0) {
                    Toast.makeText(CaseInfoNewActivity.this, "请选择评价星级", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("comment_userid", editor.get("uid", ""));
                parmas.put("commenter", editor.get("real_name", "医生"));
                parmas.put("doctor_userid", caseTo.getPatientCase().getExpert_userid());
                parmas.put("star_value", (int)(evaluation_ratingBar.getRating()) * 10 + "");
                parmas.put("comment_desc", evaluation_edit.getText().toString());
                parmas.put("case_id", caseId);
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getSaveDoctorComment(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    finish();
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        evaluation_btn.setOnTouchListener(new ButtonListener().setImage(
            getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());

        evaluation_ratingBar=(RatingBar)findViewById(R.id.case_info_new_evalution_feedback_ratingBar);

        evaluation_tip=(TextView)findViewById(R.id.case_info_new_evaluation_stars_text);
        evaluation_tip.setTextSize(17);

        evaluation_edit=(EditText)findViewById(R.id.case_info_new_evaluation_input_edit);
        evaluation_edit.setTextSize(17);
        evaluation_edit.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    starLayout.setVisibility(View.VISIBLE);
                } else {
                    starLayout.setVisibility(View.GONE);
                }
            }
        });

        discussion_edit=(EditText)findViewById(R.id.case_info_new_show_discussion_input_edit);
        discussion_edit.setTextSize(17);
        expert_dis_edit=(EditText)findViewById(R.id.case_info_new_expert_discussion_input_edit);
        expert_dis_edit.setTextSize(17);

        discussion_send_btn=(Button)findViewById(R.id.case_info_new_show_discussion_send_btn);
        discussion_send_btn.setTextSize(17);
        discussion_send_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 初级医师讨论
                if(null == discussion_edit.getText().toString() || "".equals(discussion_edit.getText().toString())) {
                    Toast.makeText(CaseInfoNewActivity.this, "请输入讨论内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("case_id", caseId);
                parmas.put("discusser_userid", editor.get("uid", ""));
                parmas.put("discusser", editor.get("real_name", "医生"));
                parmas.put("content", discussion_edit.getText().toString());
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getSendDiscussionCase(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    DiscussionTo discussionTo=new DiscussionTo();
                                    discussionTo.setCreate_time(System.currentTimeMillis());
                                    discussionTo.setCase_id(caseId);
                                    discussionTo.setDiscusser(editor.get("real_name", "医生"));
                                    discussionTo.setDiscusser_userid(editor.get("uid", ""));
                                    discussionTo.setContent(discussion_edit.getText().toString());
                                    // discussionTo.setIs_view("1");
                                    discussionTo.setHave_photos("0");
                                    UserTo userTo=new UserTo();
                                    userTo.setTp(editor.get("userType", ""));
                                    userTo.setIcon_url(editor.get("icon_url", ""));
                                    userTo.setUser_name(editor.get("real_name", ""));
                                    discussionTo.setUserTo(userTo);
                                    ImageFilesTo filesTo=new ImageFilesTo();
                                    filesTo.setCase_id(caseId);
                                    filesTo.setPic_url("");
                                    filesTo.setTest_name("");
                                    List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                                    list.add(filesTo);
                                    discussionTo.setImageFilesTos(list);
                                    addDiscussionView(true, discussionTo);
                                    discussionList.add(discussionTo);
                                    // myAdapter.notifyDataSetChanged();
                                    // setListViewHeightBasedOnChildren(discussionListView);
                                    new Handler().post(new Runnable() {

                                        @Override
                                        public void run() {
                                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                        }
                                    });
                                    discussion_edit.setText("");
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        expert_send_btn=(Button)findViewById(R.id.case_info_new_expert_discussion_btn);
        expert_send_btn.setTextSize(17);
        expert_send_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 专家讨论
                if(null == expert_dis_edit.getText().toString() || "".equals(expert_dis_edit.getText().toString())) {
                    Toast.makeText(CaseInfoNewActivity.this, "请输入讨论内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("case_id", caseId);
                parmas.put("discusser_userid", editor.get("uid", ""));
                parmas.put("discusser", editor.get("real_name", "专家"));
                parmas.put("content", expert_dis_edit.getText().toString());
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getSendDiscussionCase(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    DiscussionTo discussionTo=new DiscussionTo();
                                    discussionTo.setCreate_time(System.currentTimeMillis());
                                    discussionTo.setCase_id(caseId);
                                    discussionTo.setDiscusser(editor.get("real_name", "医生"));
                                    discussionTo.setDiscusser_userid(editor.get("uid", ""));
                                    discussionTo.setContent(expert_dis_edit.getText().toString());
                                    // discussionTo.setIs_view("1");
                                    discussionTo.setHave_photos("0");
                                    UserTo userTo=new UserTo();
                                    userTo.setTp(editor.get("userType", ""));
                                    userTo.setIcon_url(editor.get("icon_url", ""));
                                    userTo.setUser_name(editor.get("real_name", ""));
                                    discussionTo.setUserTo(userTo);
                                    ImageFilesTo filesTo=new ImageFilesTo();
                                    filesTo.setCase_id(caseId);
                                    filesTo.setPic_url("");
                                    filesTo.setTest_name("");
                                    List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                                    list.add(filesTo);
                                    discussionTo.setImageFilesTos(list);
                                    addDiscussionView(true, discussionTo);
                                    discussionList.add(discussionTo);
                                    // myAdapter.notifyDataSetChanged();
                                    // setListViewHeightBasedOnChildren(discussionListView);
                                    new Handler().post(new Runnable() {

                                        @Override
                                        public void run() {
                                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                        }
                                    });
                                    expert_dis_edit.setText("");
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        discussion_more_btn=(Button)findViewById(R.id.case_info_new_show_discussion_more_btn);
        discussion_more_btn.setTextSize(17);
        discussion_more_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 初级医师更多，根据咨询类型 专家咨询的多一个转住院或手术的功能
                if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                    // 上传图片、线下医疗服务、完成，点击完成时，弹出取消、确认框
                    Intent intent=new Intent(CaseInfoNewActivity.this, CaseMoreNewActivity.class);
                    intent.putExtra("caseId", caseId);
                    intent.putExtra("btn1", "上传图片");
                    intent.putExtra("btn2", "线下医疗服务");
                    intent.putExtra("btn3", "完成");
                    intent.putExtra("btnCount", 3);
                    startActivityForResult(intent, 1);
                } else if(caseTo.getPatientCase().getConsult_tp().equals("公开讨论")) {
                    // 上传图片、完成，点击完成时，弹出取消、确认框
                    Intent intent=new Intent(CaseInfoNewActivity.this, CaseMoreNewActivity.class);
                    intent.putExtra("caseId", caseId);
                    intent.putExtra("btn1", "上传图片");
                    intent.putExtra("btn3", "完成");
                    intent.putExtra("btnCount", 2);
                    startActivityForResult(intent, 2);
                }
            }
        });

        acceptance_btn=(Button)findViewById(R.id.case_info_new_btn_examine_ok);
        acceptance_btn.setTextSize(18);
        acceptance_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 受理
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("case_id", caseId);
                parmas.put("accept", "1");
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getExpertAccept(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    initData();
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        acceptance_btn.setOnTouchListener(new ButtonListener().setImage(
            getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());

        not_accepted_btn=(Button)findViewById(R.id.case_info_new_btn_examine_no);
        not_accepted_btn.setTextSize(18);
        not_accepted_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 不受理
                Intent intent=new Intent(CaseInfoNewActivity.this, DialogNewActivity.class);
                intent.putExtra("flag", 1);
                intent.putExtra("titleText", "请输入不受理原因");
                startActivityForResult(intent, 0);
            }
        });
        not_accepted_btn.setOnTouchListener(new ButtonListener().setImage(
            getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());

        expert_submit_btn=(Button)findViewById(R.id.case_info_new_expert_dis_btn);
        expert_submit_btn.setTextSize(18);
        expert_submit_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 专家框提交
                if((null == bcbsEdit.getText().toString() || "".equals(bcbsEdit.getText().toString()))
                    && (null == wsjcEdit.getText().toString() || "".equals(wsjcEdit.getText().toString()))
                    && (null == zdyjEdit.getText().toString() || "".equals(zdyjEdit.getText().toString()))
                    && (null == jyssEdit.getText().toString() || "".equals(jyssEdit.getText().toString()))
                    && (null == otherEdit.getText().toString() || "".equals(otherEdit.getText().toString()))) {
                    Toast.makeText(CaseInfoNewActivity.this, "请输入讨论内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer sb=new StringBuffer();
                boolean haveString=false;
                if(!(null == bcbsEdit.getText().toString() || "".equals(bcbsEdit.getText().toString()))) {
                    if(haveString) {
                        sb.append("\r\n");
                    }
                    haveString=true;
                    sb.append("补充病史：").append(bcbsEdit.getText().toString());
                }
                if(!(null == wsjcEdit.getText().toString() || "".equals(wsjcEdit.getText().toString()))) {
                    if(haveString) {
                        sb.append("\r\n");
                    }
                    haveString=true;
                    sb.append("完善检查：").append(wsjcEdit.getText().toString());
                }
                if(!(null == zdyjEdit.getText().toString() || "".equals(zdyjEdit.getText().toString()))) {
                    if(haveString) {
                        sb.append("\r\n");
                    }
                    haveString=true;
                    sb.append("诊断意见：").append(zdyjEdit.getText().toString());
                }
                if(!(null == jyssEdit.getText().toString() || "".equals(jyssEdit.getText().toString()))) {
                    if(haveString) {
                        sb.append("\r\n");
                    }
                    haveString=true;
                    sb.append("治疗建议：").append(jyssEdit.getText().toString());
                }
                if(!(null == otherEdit.getText().toString() || "".equals(otherEdit.getText().toString()))) {
                    if(haveString) {
                        sb.append("\r\n");
                    }
                    haveString=true;
                    sb.append(otherEdit.getText().toString());
                }
                Map<String, String> parmas=new HashMap<String, String>();
                parmas.put("case_id", caseId);
                parmas.put("discusser_userid", editor.get("uid", ""));
                parmas.put("discusser", editor.get("real_name", "专家"));
                parmas.put("content", sb.toString());
                final String contents=sb.toString();
                parmas.put("accessToken", ClientUtil.getToken());
                parmas.put("uid", editor.get("uid", ""));
                CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                OpenApiService.getInstance(CaseInfoNewActivity.this).getSendDiscussionCase(mQueue, parmas,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String arg0) {
                            CommonUtil.closeLodingDialog();
                            try {
                                JSONObject responses=new JSONObject(arg0);
                                if(responses.getInt("rtnCode") == 1) {
                                    DiscussionTo discussionTo=new DiscussionTo();
                                    discussionTo.setCreate_time(System.currentTimeMillis());
                                    discussionTo.setCase_id(caseId);
                                    discussionTo.setDiscusser(editor.get("real_name", "医生"));
                                    discussionTo.setDiscusser_userid(editor.get("uid", ""));
                                    discussionTo.setContent(contents);
                                    // discussionTo.setIs_view("1");
                                    discussionTo.setHave_photos("0");
                                    UserTo userTo=new UserTo();
                                    userTo.setTp(editor.get("userType", ""));
                                    userTo.setIcon_url(editor.get("icon_url", ""));
                                    userTo.setUser_name(editor.get("real_name", ""));
                                    discussionTo.setUserTo(userTo);
                                    ImageFilesTo filesTo=new ImageFilesTo();
                                    filesTo.setCase_id(caseId);
                                    filesTo.setPic_url("");
                                    filesTo.setTest_name("");
                                    List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                                    list.add(filesTo);
                                    discussionTo.setImageFilesTos(list);
                                    addDiscussionView(true, discussionTo);
                                    discussionList.add(discussionTo);
                                    // myAdapter.notifyDataSetChanged();
                                    // setListViewHeightBasedOnChildren(discussionListView);
                                    new Handler().post(new Runnable() {

                                        @Override
                                        public void run() {
                                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                        }
                                    });
                                    bcbsEdit.setText("");
                                    wsjcEdit.setText("");
                                    zdyjEdit.setText("");
                                    jyssEdit.setText("");
                                    otherEdit.setText("");
                                } else if(responses.getInt("rtnCode") == 10004) {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                    LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                        @Override
                                        public void onSuccess(String rspContent, int statusCode) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(ConsultationCallbackException exp) {
                                        }
                                    });
                                    startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                        .show();
                                }
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError arg0) {
                            CommonUtil.closeLodingDialog();
                            Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        expert_submit_btn.setOnTouchListener(new ButtonListener().setImage(
            getResources().getDrawable(R.drawable.login_login_btn_shape),
            getResources().getDrawable(R.drawable.login_login_btn_press_shape)).getBtnTouchListener());
        if(savedInstanceState != null) {
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case 0:
                    // 不受理
                    Map<String, String> parmas=new HashMap<String, String>();
                    parmas.put("case_id", caseId);
                    parmas.put("accept", "0");
                    parmas.put("accept_desc", data.getStringExtra("contentString"));
                    parmas.put("accessToken", ClientUtil.getToken());
                    parmas.put("uid", editor.get("uid", ""));
                    CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
                    OpenApiService.getInstance(CaseInfoNewActivity.this).getExpertAccept(mQueue, parmas,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String arg0) {
                                CommonUtil.closeLodingDialog();
                                try {
                                    JSONObject responses=new JSONObject(arg0);
                                    if(responses.getInt("rtnCode") == 1) {
                                        Intent intent=new Intent("com.consultation.app.new.case.action");
                                        if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                                            intent.putExtra("isOpen", 1);
                                        } else {
                                            intent.putExtra("isOpen", 0);
                                        }
                                        sendBroadcast(intent);
                                        finish();
                                    } else if(responses.getInt("rtnCode") == 10004) {
                                        Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                            .show();
                                        LoginActivity.setHandler(new ConsultationCallbackHandler() {

                                            @Override
                                            public void onSuccess(String rspContent, int statusCode) {
                                                initData();
                                            }

                                            @Override
                                            public void onFailure(ConsultationCallbackException exp) {
                                            }
                                        });
                                        startActivity(new Intent(CaseInfoNewActivity.this, LoginActivity.class));
                                    } else {
                                        Toast.makeText(CaseInfoNewActivity.this, responses.getString("rtnMsg"), Toast.LENGTH_SHORT)
                                            .show();
                                    }
                                } catch(JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError arg0) {
                                CommonUtil.closeLodingDialog();
                                Toast.makeText(CaseInfoNewActivity.this, "网络连接失败,请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    break;
                case 1:
                    if(data.getStringExtra("status").equals("3")) {
                        Intent intent=new Intent("com.consultation.app.new.case.action");
                        if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                            intent.putExtra("isOpen", 1);
                        } else {
                            intent.putExtra("isOpen", 0);
                        }
                        sendBroadcast(intent);
                        finish();
                    } else if(data.getStringExtra("status").equals("2")) {
                        Intent intent=new Intent("com.consultation.app.new.case.action");
                        if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                            intent.putExtra("isOpen", 1);
                        } else {
                            intent.putExtra("isOpen", 0);
                        }
                        sendBroadcast(intent);
                        finish();
                    } else if(data.getStringExtra("status").equals("1")) {
                        uploadImage(data.getStringExtra("path"));
                    }
                    break;
                case 2:
                    if(data.getStringExtra("status").equals("3")) {
                        Intent intent=new Intent("com.consultation.app.new.case.action");
                        if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                            intent.putExtra("isOpen", 1);
                        } else {
                            intent.putExtra("isOpen", 0);
                        }
                        sendBroadcast(intent);
                        finish();
                    } else if(data.getStringExtra("status").equals("1")) {
                        uploadImage(data.getStringExtra("path"));
                    }
                    break;
                case 3:
                    Intent intent=new Intent("com.consultation.app.new.case.action");
                    if(caseTo.getPatientCase().getConsult_tp().equals("专家咨询")) {
                        intent.putExtra("isOpen", 1);
                    } else {
                        intent.putExtra("isOpen", 0);
                    }
                    sendBroadcast(intent);
                    finish();
                    break;

                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage(final String photoPath) {
        File[] files=new File[1];
        File file=new File(photoPath);
        files[0]=file;
        Map<String, String> params=new HashMap<String, String>();
        params.put("case_id", caseId);
        params.put("discusser_userid", editor.get("uid", ""));
        params.put("discusser", editor.get("real_name", "医生"));
        params.put("accessToken", ClientUtil.getToken());
        params.put("uid", editor.get("uid", ""));
        CommonUtil.showLoadingDialog(CaseInfoNewActivity.this);
        OpenApiService.getInstance(CaseInfoNewActivity.this).getUploadFiles(ClientUtil.GET_DISCUSSION_CASE_IMAGE_URL,
            CaseInfoNewActivity.this, new ConsultationCallbackHandler() {

                @Override
                public void onSuccess(String rspContent, int statusCode) {
                    CommonUtil.closeLodingDialog();
                    DiscussionTo discussionTo=new DiscussionTo();
                    discussionTo.setCreate_time(System.currentTimeMillis());
                    discussionTo.setCase_id(caseId);
                    discussionTo.setDiscusser(editor.get("real_name", "医生"));
                    discussionTo.setDiscusser_userid(editor.get("uid", ""));
                    // discussionTo.setIs_view("1");
                    discussionTo.setHave_photos("1");
                    UserTo userTo=new UserTo();
                    userTo.setTp(editor.get("userType", ""));
                    userTo.setIcon_url(editor.get("icon_url", ""));
                    userTo.setUser_name(editor.get("real_name", ""));
                    discussionTo.setUserTo(userTo);
                    ImageFilesTo filesTo=new ImageFilesTo();
                    filesTo.setCase_id(caseId);
                    filesTo.setPic_url(photoPath);
                    filesTo.setLittle_pic_url(photoPath);
                    filesTo.setTest_name("");
                    List<ImageFilesTo> list=new ArrayList<ImageFilesTo>();
                    list.add(filesTo);
                    discussionTo.setImageFilesTos(list);
                    addDiscussionView(true, discussionTo);
                    discussionList.add(discussionTo);
                    // myAdapter.notifyDataSetChanged();
                    // setListViewHeightBasedOnChildren(discussionListView);
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }

                @Override
                public void onFailure(ConsultationCallbackException exp) {
                    CommonUtil.closeLodingDialog();
                    Toast.makeText(CaseInfoNewActivity.this, "发送失败", Toast.LENGTH_LONG).show();
                }
            }, files, params);
    }

    // public void setListViewHeightBasedOnChildren(ListView listView) {
    // ListAdapter listAdapter=listView.getAdapter();
    // if(listAdapter == null) {
    // return;
    // }
    // int totalHeight=0;
    // for(int i=0; i < listAdapter.getCount(); i++) {
    // View listItem=listAdapter.getView(i, null, listView);
    // listItem.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
    // MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    // totalHeight+=listItem.getMeasuredHeight();
    // }
    // ViewGroup.LayoutParams params=listView.getLayoutParams();
    // params.height=totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
    // listView.setLayoutParams(params);
    // }
    //
    // private class ViewHolder {
    //
    // TextView contents;
    //
    // TextView name;
    //
    // ImageView photo;
    //
    // TextView title;
    //
    // TextView create_time;
    //
    // ImageView imageView;
    //
    // }
    //
    // private class MyAdapter extends BaseAdapter {
    //
    // @Override
    // public int getCount() {
    // return discussionList.size();
    // }
    //
    // @Override
    // public Object getItem(int location) {
    // return discussionList.get(location);
    // }
    //
    // @Override
    // public long getItemId(int position) {
    // return position;
    // }
    //
    // @SuppressLint("NewApi")
    // @Override
    // public View getView(int position, View convertView, ViewGroup parent) {
    // if(convertView == null) {
    // holder=new ViewHolder();
    // if(discussionList.get(position).getDiscusser_userid().equals(editor.get("uid", ""))) {
    // convertView=
    // LayoutInflater.from(CaseInfoNewActivity.this).inflate(R.layout.discussion_case_mine_list_item, null);
    // } else {
    // convertView=LayoutInflater.from(CaseInfoNewActivity.this).inflate(R.layout.discussion_case_list_item, null);
    // }
    // holder.contents=(TextView)convertView.findViewById(R.id.discussion_case_item_content);
    // holder.create_time=(TextView)convertView.findViewById(R.id.discussion_case_item_createTime);
    // holder.name=(TextView)convertView.findViewById(R.id.discussion_case_item_name);
    // holder.imageView=(ImageView)convertView.findViewById(R.id.discussion_case_item_content_imageView);
    // holder.title=(TextView)convertView.findViewById(R.id.discussion_case_item_title);
    // holder.photo=(ImageView)convertView.findViewById(R.id.discussion_case_item_photo);
    // convertView.setTag(holder);
    // } else {
    // holder=(ViewHolder)convertView.getTag();
    // }
    // if(discussionList.get(position).getHave_photos().equals("1")) {
    // holder.contents.setVisibility(View.GONE);
    // holder.imageView.setVisibility(View.VISIBLE);
    // final String imgUrl=discussionList.get(position).getImageFilesTos().get(0).getLittle_pic_url();
    // final String bigImgUrl=discussionList.get(position).getImageFilesTos().get(0).getPic_url();
    // if(!"null".equals(imgUrl) && !"".equals(imgUrl)) {
    // if(imgUrl.startsWith("http://")) {
    // holder.imageView.setImageResource(R.anim.loading_anim_test);
    // holder.imageView.setTag(imgUrl);
    // ImageListener listener=ImageLoader.getImageListener(holder.imageView, 0, android.R.drawable.ic_menu_delete);
    // mImageLoader.get(imgUrl, listener, 200, 200);
    // } else {
    // Bitmap bitmap=CommonUtil.readBitMap(200, imgUrl);
    // holder.imageView.setImageBitmap(bitmap);
    // }
    // holder.imageView.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // BigImageActivity.setViewData(bigImgUrl);
    // startActivity(new Intent(CaseInfoNewActivity.this, BigImageActivity.class));
    // }
    // });
    // }
    // } else {
    // holder.contents.setVisibility(View.VISIBLE);
    // holder.imageView.setVisibility(View.GONE);
    // holder.contents.setText(discussionList.get(position).getContent());
    // holder.contents.setTextSize(18);
    // }
    // SimpleDateFormat sdf=new SimpleDateFormat("MM-dd  HH:mm");
    // String sd=sdf.format(new Date(discussionList.get(position).getCreate_time()));
    // holder.create_time.setText(sd);
    // holder.create_time.setTextSize(15);
    // holder.title.setText("初级医师");
    // holder.title.setBackgroundResource(R.drawable.discussion_title_shape);
    // if(discussionList.get(position).getUserTo().getTp().equals("2")) {
    // holder.title.setText("专家");
    // holder.title.setBackgroundResource(R.drawable.discussion_mine_title_shape);
    // }
    // holder.title.setTextSize(15);
    // holder.name.setText(discussionList.get(position).getDiscusser());
    // holder.name.setTextSize(15);
    // final String imgUrl=discussionList.get(position).getUserTo().getIcon_url();
    // holder.photo.setTag(imgUrl);
    // int photoId=0;
    // if(discussionList.get(position).getUserTo().getTp().equals("1")) {
    // photoId=R.drawable.photo_primary;
    // } else if(discussionList.get(position).getUserTo().getTp().equals("2")) {
    // photoId=R.drawable.photo_expert;
    // }
    // holder.photo.setImageResource(photoId);
    // if(!"null".equals(imgUrl) && !"".equals(imgUrl)) {
    // ImageListener listener=ImageLoader.getImageListener(holder.photo, photoId, photoId);
    // mImageLoader.get(imgUrl, listener);
    // }
    // return convertView;
    // }
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadDisable=true;
    }
}
