package io.techery.janet;

/**
 * Wrapper for interception lifecycle of delegated {@link ActionService}
 */
public abstract class ActionServiceWrapper extends ActionService {

    private final ActionService actionService;

    public ActionServiceWrapper(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * Called before action sending
     * @param holder action holder for intercepting
     * @return if {@code true} action won't be processed by decorated service neither any status callback is called
     */
    protected abstract <A> boolean onInterceptSend(ActionHolder<A> holder);

    /**
     * Called before action cancellation
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptCancel(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#START START}
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptStart(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#PROGRESS PROGRESS}
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptProgress(ActionHolder<A> holder, int progress);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#SUCCESS SUCCESS}
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptSuccess(ActionHolder<A> holder);

    /**
     * Called from service callback before changing action status to {@linkplain ActionState.Status#FAIL FAIL}
     * @param holder action holder for intercepting
     */
    protected abstract <A> void onInterceptFail(ActionHolder<A> holder, JanetException e);

    /**
     * {@inheritDoc}
     */
    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        boolean intercepted = onInterceptSend(holder);
        if (!intercepted) {
            actionService.sendInternal(holder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override protected <A> void cancel(ActionHolder<A> holder) {
        onInterceptCancel(holder);
        actionService.cancel(holder);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected Class getSupportedAnnotationType() {
        return actionService.getSupportedAnnotationType();
    }

    /**
     * {@inheritDoc}
     */
    @Override void setCallback(Callback callback) {
        callback = new CallbackWrapper(callback, interceptor);
        super.setCallback(callback);
        actionService.setCallback(callback);
    }

    private final CallbackWrapper.Interceptor interceptor = new CallbackWrapper.Interceptor() {
        @Override public <A> void interceptStart(ActionHolder<A> holder) {
            onInterceptStart(holder);
        }

        @Override public <A> void interceptProgress(ActionHolder<A> holder, int progress) {
            onInterceptProgress(holder, progress);
        }

        @Override public <A> void interceptSuccess(ActionHolder<A> holder) {
            onInterceptSuccess(holder);
        }

        @Override public <A> void interceptFail(ActionHolder<A> holder, JanetException e) {
            onInterceptFail(holder, e);
        }
    };
}
