package com.zkrypto.cryptolib;

public class TssBridge {
    public static native void participantFactory(
            String participantType,
            long sid,
            long id,
            long[] otherParticipantIds,
            String input
    );

    public static native String readyMessageFactory(
            String participantType,
            long sid,
            long myId
    );

    public static native String delegateProcessMessage(
            String participantType,
            String message
    );

    public static native String generateTshareInput(
            String auxinfoOutput,
            int threshold
    );

    public static native String generateTrefreshInput(
            String tshareOutput,
            String auxinfoOutput,
            int threshold
    );

    public static native String generateTpresignInput(
            long[] signerParticipantIds,
            String auxinfoOutput,
            String tshareOutput
    );

    public static native String generateSignInput(
            long myId,
            long[] signerParticipantIds,
            String tpresignOutput,
            String tshareOutput,
            byte[] messageBytes,
            int threshold
    );

    public static native String generateTrecoverHelperInput(
            long[] helperParticipantIds,
            long myParticipantId,
            long targetParticipantId,
            String tshareOutput,
            String auxinfoOutput,
            int threshold
    );

    public static native String generateTrecoverTargetInput(
            long[] helperParticipantIds,
            String auxinfoOutput,
            int threshold
    );

    public static native String computeOutput(
            String[] messages
    );
}