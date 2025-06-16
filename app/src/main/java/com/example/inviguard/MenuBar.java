package com.example.inviguard;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    // 기본 생성자 (모든 Activity에서 공통 사용)
    public MenuBar(DrawerLayout drawerLayout, ImageView menuButton, TextView menuClose) {
        this.drawerLayout = drawerLayout;
        this.menuButton = menuButton;
        this.menuClose = menuClose;
    }

    // 메뉴바 리스너 인터페이스 정의
    public interface MenuBarListener {
        void onMenuOpened();
    }

    // 리스너 등록
    public void setMenuBarListener(MenuBarListener listener) {
        this.listener = listener;
    }

    // 메뉴 기능 초기화 (클릭 리스너와 드로어 애니메이션을 설정)
    public void setupMenu() {
        try {
            setupClickListeners();
            setupDrawerAnimation();
            setupOverlayEffect();
            Log.d("MenuBar", "메뉴 설정 완료");
        } catch (Exception e) {
            Log.e("MenuBar", "메뉴 설정 중 오류", e);
        }
    }

    // 메뉴 버튼 클릭 리스너 설정
    private void setupClickListeners() {
        try {
            if (menuButton != null) {
                // 햄버거 버튼 클릭 → 메뉴 열기
                menuButton.setOnClickListener(v -> {
                    try {
                        if (drawerLayout != null) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        }
                    } catch (Exception e) {
                        Log.e("MenuBar", "메뉴 열기 중 오류", e);
                    }
                });
            }

            if (menuClose != null) {
                // 닫기 버튼 클릭 → 메뉴 닫기
                menuClose.setOnClickListener(v -> {
                    try {
                        if (drawerLayout != null) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    } catch (Exception e) {
                        Log.e("MenuBar", "메뉴 닫기 중 오류", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("MenuBar", "클릭 리스너 설정 중 오류", e);
        }
    }

    // 드로어 애니메이션 효과 설정
    private void setupDrawerAnimation() {
        try {
            if (drawerLayout != null) {
                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        try {
                            // 메뉴가 열릴 때 메인 콘텐츠의 투명도 조절
                            float alpha = 1.0f - (slideOffset * 0.2f);
                            View mainContent = drawerLayout.getChildAt(0);
                            if (mainContent != null) {
                                mainContent.setAlpha(alpha);
                            }
                        } catch (Exception e) {
                            Log.e("MenuBar", "드로어 슬라이드 중 오류", e);
                        }
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        try {
                            // 메뉴가 완전히 열렸을 때
                            View mainContent = drawerLayout.getChildAt(0);
                            if (mainContent != null) {
                                mainContent.setAlpha(0.8f);
                            }

                            // 메뉴 열릴 때 외부 로직 실행 (MenuBarListener)
                            if (listener != null) {
                                listener.onMenuOpened();
                            }
                        } catch (Exception e) {
                            Log.e("MenuBar", "드로어 열기 완료 중 오류", e);
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        try {
                            // 메뉴가 완전히 닫혔을 때
                            View mainContent = drawerLayout.getChildAt(0);
                            if (mainContent != null) {
                                mainContent.setAlpha(1.0f);
                            }
                        } catch (Exception e) {
                            Log.e("MenuBar", "드로어 닫기 완료 중 오류", e);
                        }
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // 드로어 상태 변경 시 (필요시 추가 로직 구현)
                    }
                });
            }
        } catch (Exception e) {
            Log.e("MenuBar", "드로어 애니메이션 설정 중 오류", e);
        }
    }

    // 오버레이 효과 설정
    private void setupOverlayEffect() {
        try {
            if (drawerLayout != null) {
                drawerLayout.setScrimColor(Color.parseColor("#80000000"));
            }
        } catch (Exception e) {
            Log.e("MenuBar", "오버레이 효과 설정 중 오류", e);
        }
    }

    // 메뉴 상태 확인 메소드
    public boolean isMenuOpen() {
        try {
            if (drawerLayout != null) {
                return drawerLayout.isDrawerOpen(GravityCompat.START);
            }
        } catch (Exception e) {
            Log.e("MenuBar", "메뉴 상태 확인 중 오류", e);
        }
        return false;
    }

    // 메뉴 강제 닫기 메소드
    public void closeMenu() {
        try {
            if (drawerLayout != null && isMenuOpen()) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        } catch (Exception e) {
            Log.e("MenuBar", "메뉴 강제 닫기 중 오류", e);
        }
    }
}