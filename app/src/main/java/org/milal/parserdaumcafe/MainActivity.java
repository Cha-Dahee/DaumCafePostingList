package org.milal.parserdaumcafe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import static android.R.attr.description;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;


public class MainActivity extends Activity {
    EditText editText;
    ListView listView;
    ArrayList<String> list; //listview에 연결할 모델 객체
    ArrayAdapter<String> adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText)findViewById(R.id.keyword);
        listView = (ListView)findViewById(R.id.listView);
        list = new ArrayList<String>();
        //리스트뷰에 모델객체를 연결할 아답타 객체
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        //리스트뷰에 아답타 연결하기
        listView.setAdapter(adapter);
        listView.setDivider(new ColorDrawable(Color.RED));
        listView.setDividerHeight(3); //구분선
        listView.setOnItemClickListener(itemClickListenerOfSearchResult);



    }
    //버튼을 눌렀을때 콜백 메소드
    public void find(View v) throws Exception{
        //버튼이 눌릴때마다 데이터가 쌓이는 것을 방지하기 위해
        list.clear();
        //요청 url 만들기
        String keyWord = editText.getText().toString();
        Log.d("keyWord?",keyWord);
        //한글이 깨지지 않게 하기 위해
        String encodedK = "\""+URLEncoder.encode(keyWord, "utf-8")+"\"+";
        String defaultOp = "\""+URLEncoder.encode("휠체어","utf-8")+"\"";

        StringBuffer buffer = new StringBuffer();
        buffer.append("http://apis.daum.net/search/cafe?result=20&");
        //한글일 경우 인코딩 필요!(영어로 가정한다)
        buffer.append("q="+encodedK+defaultOp);
        buffer.append("&apikey=d847f0a73b32bb8fa47db90e8d71f7dd");
        buffer.append("&output=json");

        String url = buffer.toString();
        Log.d("url?",url);
        //스레드 객체를 생성해서 다운로드 받는다.
        GetJSONThread thread = new GetJSONThread(handler, null, url);
        thread.start();
}

    //핸들러
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case 0 : //success
                    Toast.makeText(MainActivity.this, "성공?", 0).show();
                    //json문자열을 ㅡ읽어오기
                    String jsonStr = (String)msg.obj;
                    try{
                        //문자열을 json 객체로 변환
                        //1. channel이라는 키값으로 {} jsonObject가 들어있다)
                        //2. jsonObject안에는 item이라는 키값으로 [] jsonArray 벨류값을 가지고 있다.
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        //1.
                        JSONObject channel = jsonObj.getJSONObject("channel");
                        Log.d("result:", String.valueOf(channel.getString("result")));
                        Log.d("pageCount:", String.valueOf(channel.getString("pageCount")));
                        Log.d("totalCount:", String.valueOf(channel.getString("totalCount")));

                        //2.
                        JSONArray items = channel.getJSONArray("item");
                        //3.반복문 돌면서 필요한 정보만 얻어온다.
                        for(int i=0 ; i<items.length() ; i++){
                            Log.d("몇 번째?",String.valueOf(i));
                            //4. 검색결과 값을 얻어온다.
                            JSONObject tmp = items.getJSONObject(i);
                            String cafeName = tmp.getString("cafeName");

                            if(!(cafeName.equalsIgnoreCase("휠체어배낭여행(장애인여행)")||cafeName.equalsIgnoreCase("휠체어로 세계로...")))
                                continue;
                            String title = tmp.getString("title");
                            String link = tmp.getString("link");
                            list.add(title + "\n" + link + "\n" + cafeName);
                        }
                        //모델의 데이터가 바뀌었다고 아답타 객체에 알린다.
                        adapter.notifyDataSetChanged();
                    }catch (Exception e) {

                    }

                    break;

                case 1 : //fail

                    break;

            }
        }
    };

    private AdapterView.OnItemClickListener itemClickListenerOfSearchResult = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
            String str = ((TextView)clickedView).getText().toString();
            String cafeLink = str.split("\n")[1];
            Intent CafePosting = new Intent(Intent.ACTION_VIEW, Uri.parse(cafeLink));
            // Intent CafePosting = new Intent(getApplicationContext(), ShowCafePosting.class);
            startActivity(CafePosting);
        }
    };
}

