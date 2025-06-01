package com.example.inviguard;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

// 메뉴바 클래스 구현
// 메인 전, 후에 모두 필요해서 분리해놓은 코드
public class MenuBar {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private TextView menuClose;
    private MenuBarListener listener;

    // 메뉴 헬퍼 생성자
    // @param drawerLayout 드로어 레이아웃
    // @param menuButton 메뉴 열기 버튼
    // @param menuClose 메뉴 닫기 버튼
    public MenuBar(DrawerLayout drawerLayout, ImageView menuButton, TextView menuClose) {
        this.drawerLayout = drawerLayout;
        this.menuButton = menuButton;
        this.menuClose = menuClose;
    }

    // 리스너 등록
    public void setMenuBarListener(MenuBarListener listener) {
        this.listener = listener;
    }

    // 메뉴 기능 초기화 (클릭 리스너와 드로어 애니메이션을 설정)
    public void setupMenu() {
        setupClickListeners();
        setupDrawerAnimation();
        setupOverlayEffect();
    }

    // 메뉴 버튼 클릭 리스너 설정
    private void setupClickListeners() {
        // 햄버거 버튼 클릭 → 메뉴 열기
        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 닫기 버튼 클릭 → 메뉴 닫기
        menuClose.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    // 드로어 애니메이션 효과 설정
    private void setupDrawerAnimation() {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // 메뉴가 열릴 때 메인 콘텐츠의 투명도 조절
                float alpha = 1.0f - (slideOffset * 0.2f);
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(alpha);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // 메뉴가 완전히 열렸을 때
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(0.8f);

                if (listener != null) {
                    listener.onMenuOpened(); // 메뉴 열릴 때 외부 로직 실행 (MenuBarListener)
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // 메뉴가 완전히 닫혔을 때
                View mainContent = drawerLayout.getChildAt(0);
                mainContent.setAlpha(1.0f);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // 드로어 상태 변경 시 (필요시 추가 로직 구현)
            }
        });
    }

    // 오버레이 효과 설정
    private void setupOverlayEffect() {
        drawerLayout.setScrimColor(Color.parseColor("#80000000"));
    }
}
