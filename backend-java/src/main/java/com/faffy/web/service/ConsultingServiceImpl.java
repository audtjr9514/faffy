package com.faffy.web.service;

import com.faffy.web.dto.ConsultingCreateDto;
import com.faffy.web.dto.ConsultingGetDto;
import com.faffy.web.dto.HistoryConsultingDto;
import com.faffy.web.exception.IllegalInputException;
import com.faffy.web.jpa.entity.*;
import com.faffy.web.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultingServiceImpl implements ConsultingService {
    @Autowired
    ConsultingRepository consultingRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FashionCategoryRepository fashionCategoryRepository;
    @Autowired
    ConsultingCategoryRepository consultingCategoryRepository;
    @Autowired
    ConsultingLogRepository consultingLogRepository;

    private String getDuration(String startTime, String endTime){
        System.out.println("startTime:"+startTime);
        System.out.println("endTime:"+endTime);
        String[] sTime = startTime.substring(0, startTime.length()-2).split(":");
        String[] eTime = endTime.substring(0, startTime.length()-2).split(":");
        int stotal = 60*60*Integer.parseInt(sTime[0]) + 60*Integer.parseInt(sTime[1]) + Integer.parseInt(sTime[2]);
        int etotal = 60*60*Integer.parseInt(eTime[0]) + 60*Integer.parseInt(eTime[1]) + Integer.parseInt(eTime[2]);
        int dur = etotal - stotal;

        int hour = dur/3600, minute = (dur%3600)/60, second = dur%60;
        return hour + ":" + minute + ":" + second;
    }

    @Override
    public HistoryConsultingDto getHistoryConsulting(int no) throws IllegalInputException {
        Consulting consulting = consultingRepository.findById(no).orElse(null);
        if(consulting == null)
            throw new IllegalInputException();

        Timestamp startTimestamp = Timestamp.valueOf(consulting.getStartTime());
        Timestamp endTimestamp = Timestamp.valueOf(consulting.getEndTime());
        String sdate = startTimestamp.toString().split(" ")[0].replace(':', '-');

        String startTime = startTimestamp.toString().split(" ")[1];
        String endTime = endTimestamp.toString().split(" ")[1];
        String duration = getDuration(startTime, endTime);

        return HistoryConsultingDto.builder()
                .no(consulting.getNo())
                .title(consulting.getTitle())
                .date(sdate)
                .duration(duration)
                .startTime(startTime)
                .endTime(endTime)
                .introduce(consulting.getIntro())
                .categories(consulting.getCategories())
                .fileList(consulting.getSnapshots())
                .build();
    }

    @Override
    public File getSnapshot(int no) throws IllegalInputException{
        UploadFile uf = uploadFileRepository.findById(no).orElse(null);
        if(uf == null)
            throw new IllegalInputException();

        String filename = uf.getUploadPath() + File.separator + uf.getUuid() + "_" + uf.getFileName();
        return new File(filename);
    }

    @Override
    public List<ConsultingGetDto> getConsultingsByViewCount(Pageable pageable) {
        List<Consulting> consultings = consultingRepository.findAllOrderByViewCount(pageable);
        List<ConsultingGetDto> dtoList = new ArrayList<>();
        for(Consulting c : consultings){
            dtoList.add(c.toConsultingGetDto());
        }
        return dtoList;
    }

    @Override
    @Transactional
    public ConsultingGetDto createConsulting(ConsultingCreateDto dto, int no) {
        User user = userRepository.findByNo(no).orElse(null);
        if(user == null)
            return null;

        Consulting consulting = Consulting.builder()
                .consultant(user)
                .title(dto.getTitle())
                .intro(dto.getIntroduce())
                .roomSize(dto.getRoomSize())
                .startTime(LocalDateTime.now())
                .build();

        consultingRepository.save(consulting);
        for(String c : dto.getCategories()){
            FashionCategory fashionCategory = fashionCategoryRepository.findByName(c).orElse(null);
            if(fashionCategory == null) //저장되지 않은 카테고리면 설정하지 않음
                continue;

            ConsultingCategory cc = ConsultingCategory.builder().category(fashionCategory).consulting(consulting).build();
            consultingCategoryRepository.save(cc);
        }
//        return consulting.toConsultingGetDto();
        return ConsultingGetDto.builder().build(); // 성공시 빈 객체 반환
    }

    @Override
    public void createLog(int consulting_no, int user_no) throws IllegalInputException{
        Consulting consulting = consultingRepository.findById(consulting_no).orElse(null);
        User user = userRepository.findByNo(user_no).orElse(null);
        if(consulting == null || user == null){
            throw new IllegalInputException();
        }

        ConsultingLog log = ConsultingLog.builder().consulting(consulting).user(user).build();
        consultingLogRepository.save(log);
    }

    @Override
    @Transactional
    public void setViewCount(int no, int cnt) throws IllegalInputException {
        Consulting consulting = consultingRepository.findById(no).orElse(null);
        if(consulting == null)
            throw new IllegalInputException();

        consulting.setViewCount(cnt);
    }
}
