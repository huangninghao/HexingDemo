package com.hexing.hexingdemo.presenter;


import com.hexing.hexingdemo.contact.TestContact;
import com.hexing.libhexbase.inter.RxBasePresenterImpl;


/*
 * 项目名:    BaseFrame
 * 文件名:    TestPresenter
 * 创建时间:  2017/9/7 on 11:17
 * 描述:     TODO
 */
public class TestPresenter extends RxBasePresenterImpl<TestContact.view> implements TestContact.presenter {

    public TestPresenter(TestContact.view view) {
        super(view);
    }

    /**
     * 获取数据
     */
    @Override
    public void getData() {
//        Api.getInstance().test()
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(new Consumer<Disposable>() {
//                    @Override
//                    public void accept(@NonNull Disposable disposable) throws Exception {
//                        addDisposable(disposable);
//                        mvpView.get().showLoadingDialog("");
//                    }
//                })
//                .map(new Function<TestBean, List<TestBean.StoriesBean>>() {
//                    @Override
//                    public List<TestBean.StoriesBean> apply(@NonNull TestBean testBean) throws Exception {
//                        return testBean.getStories();
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<TestBean.StoriesBean>>() {
//                    @Override
//                    public void accept(@NonNull List<TestBean.StoriesBean> storiesBeen) {
//                        mvpView.get().hideLoadingDialog();
//                        mvpView.get().setData(storiesBeen);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(@NonNull Throwable throwable) throws  Exception{
//                        mvpView.get().hideLoadingDialog();
//                        RxExceptionHelper.handleException(throwable);
//                    }
//                });
    }
}
