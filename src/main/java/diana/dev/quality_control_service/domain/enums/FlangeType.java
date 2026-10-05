package diana.dev.quality_control_service.domain.enums;

public enum FlangeType {
    FLAT,           // Плоский приварной
    WELD_NECK,      // Воротниковый приварной встык
    THREADED,       // Резьбовой
    LAP_JOINT,      // Свободный на приварном кольце
    BLIND,          // Глухой
    INTEGRAL,       // Соединительный
    UNKNOWN
}
