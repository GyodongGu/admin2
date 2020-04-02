package admin.shoes.app.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import admin.shoes.app.dto.DayStatisticsDTO;
import admin.shoes.app.dto.MonthStatisticsDTO;
import admin.shoes.app.dto.SexRatioDTO;
import admin.shoes.app.dto.StatisticsDTO;
import admin.shoes.app.dto.WeekStatisticsDTO;
import admin.shoes.app.dto.imageDetailDTO;
import admin.shoes.app.dto.ordDTO;
import admin.shoes.app.dto.pdtDTO;
import admin.shoes.app.dto.pdtStatisticsDTO;

/**
 * 
 * @author 구교동
 * 1.
 * 
 * @author 유승우
 * 2. 관리자 YouShoes의 총 매출   YouShoesStatistics()
 * 3. 판매 회원별 연 매출  sMemStatistics()
 * 4. 판매 회원별 월 매출  sMonthStatistics()
 * 5. 판매 회원별 주 매출  sWeekStatistics()
 * 6. 판매 회원별 일 매출  sDayStatistics()
 * 7. 판매 회원별 판매 품목 sProductStatistics()
 * 8. 판매 회원별 구매한 성비 sSexStatistics()
 */

public class ordDAO extends DAO{
	// 1.
	public List<ordDTO> ordList(int pmno){
		List<ordDTO> list = new ArrayList<ordDTO>();
		
		String sql = "select * from ord where pm_no=?";
		String sql1 = "select * from product where pdt_no=?";
		String sql2="select img_name, i.img_no from image i join image_detail d on i.img_no=d.img_no where section='I02' and section_no=?";
		PreparedStatement psmt1;
		ResultSet rs1;
		
		PreparedStatement psmt2;
		ResultSet rs2;
		
		try {
			psmt=conn.prepareStatement(sql);
			psmt.setInt(1, pmno);
			
			rs=psmt.executeQuery();
			
			while(rs.next()) {
				ordDTO odto = new ordDTO();
				odto.setOrd_no(rs.getInt("ord_no"));
				odto.setPm_no(rs.getInt("pm_no"));
				odto.setPdt_no(rs.getInt("pdt_no"));
				odto.setOrd_date(rs.getDate("ord_date"));
				odto.setOrd_point(rs.getInt("ord_point"));
				odto.setOrd_stat_cd(rs.getString("ord_stat_cd"));
				
				psmt1=conn.prepareStatement(sql1);
				psmt1.setInt(1, rs.getInt("pdt_no"));
				rs1=psmt1.executeQuery();
				if(rs1.next()) {
					pdtDTO pdto = new pdtDTO();
					pdto.setPdt_name(rs1.getString("pdt_name"));
					
					psmt2=conn.prepareStatement(sql2);
					psmt2.setInt(1, rs.getInt("pdt_no"));
					rs2=psmt2.executeQuery();
					List<imageDetailDTO> imgList=new ArrayList<imageDetailDTO>();
					while(rs2.next()) {
						imageDetailDTO imgDTO = new imageDetailDTO();
						imgDTO.setImg_name(rs2.getString("img_name"));
						imgDTO.setImg_no(rs2.getInt("img_no"));
						imgList.add(imgDTO);
					}
					pdto.setImg_name(imgList);
					odto.setPdto(pdto);
				}
				
				list.add(odto);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}

	// 2. 관리자 YouShoes의 총 매출   YouShoesStatistics()
	public List<StatisticsDTO> YouShoesStatistics() {
		List<StatisticsDTO> list = new ArrayList<StatisticsDTO>();
		String sql = "select DATE_FORMAT(ord_date, '%Y') y, sum(ord_detail_point) as sumOrd " 
				   + "from ord o join ord_detail od " 
				   + "on o.ord_no = od.ord_no " 
				   + "where DATE_ADD(sysdate(), INTERVAL - 60 MONTH) < ord_date "
				   + "group by DATE_FORMAT(ord_date, '%Y') "
				   + "order by 1";
		try {
			psmt = conn.prepareStatement(sql);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				StatisticsDTO sdto = new StatisticsDTO();
				sdto.setSumOrd(rs.getInt("sumOrd"));
				sdto.setYear(rs.getString("y"));
				
				list.add(sdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	// 3. 판매 회원별 연 매출  sMemStatistics()
	public List<StatisticsDTO> sMemStatistics(String id) {
		List<StatisticsDTO> list = new ArrayList<StatisticsDTO>();
		String sql = "select DATE_FORMAT(ord_date, '%Y') y, sum(ord_detail_point) as sumOrd " 
				   + "from ord o, ord_detail od,  product p " 
				   + "where sm_id = ? " 
				   + "and o.ord_no = od.ord_no " 
				   + "and o.pdt_no = p.pdt_no "
				   + "and DATE_ADD(sysdate(), INTERVAL - 60 MONTH) < ord_date " 
				   + "group by DATE_FORMAT(ord_date, '%Y') "
				   + "order by 1";
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, id);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				StatisticsDTO sdto = new StatisticsDTO();
				sdto.setSumOrd(rs.getInt("sumOrd"));
				sdto.setYear(rs.getString("y"));
				
				list.add(sdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	// 4. 판매 회원별 월 매출  sMonthStatistics()
	public List<MonthStatisticsDTO> sMonthStatistics(int varYearSelect, String id) {
		List<MonthStatisticsDTO> list = new ArrayList<MonthStatisticsDTO>();
		String sql = "select lmon, IFNULL(mon.sumord,0) as sumord " 
					+ "from (select DATE_FORMAT(ord_date, '%Y%m') as y, sum(ord_detail_point) as sumOrd " 
					+ "        from ord o, ord_detail od,  product p " 
					+ "        where DATE_ADD(sysdate(), INTERVAL - 60 MONTH) < ord_date " 
					+ "        and DATE_FORMAT(ord_date, '%Y') = ? " 
					+ "        and sm_id = ? " 
					+ "        and o.ord_no = od.ord_no " 
					+ "        and o.pdt_no = p.pdt_no " 
					+ "        group by DATE_FORMAT(ord_date, '%Y%m') " 
					+ "     ) mon " 
					+ "     right outer join " 
					+ "     (SELECT CONCAT(?,no) as lmon FROM monthstatistics" 
					+ " 	) LS " 
					+ "on y = lmon " 
					+ "order by lmon";
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setInt(1, varYearSelect);
			psmt.setString(2, id);
			psmt.setInt(3, varYearSelect);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				MonthStatisticsDTO msdto = new MonthStatisticsDTO();
				msdto.setLmon(rs.getString("lmon"));
				msdto.setSumord(rs.getInt("sumord"));
				list.add(msdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	
	// 5. 판매 회원별 주 매출  sWeekStatistics()
	public List<WeekStatisticsDTO> sWeekStatistics(String id, int varWeekSelect) {
		List<WeekStatisticsDTO> list = new ArrayList<WeekStatisticsDTO>();
		String sql = "select concat(lmon,'주') as lmon, IFNULL(mon.sumord,0) as sumord " 
					+ "from (select (select floor((date_format(ord_date,'%d')+(date_format(date_format(ord_date,'%Y%m%01'),'%w')-1))/7)+1 credate) as y, sum(ord_detail_point) as sumOrd " 
					+ "        from ord o, ord_detail od,  product p " 
					+ "        where " 
					+ "        DATE_FORMAT(ord_date, '%Y%m') = ? " 
					+ "        and sm_id = ? " 
					+ "        and o.ord_no = od.ord_no " 
					+ "        and o.pdt_no = p.pdt_no " 
					+ "        group by (select floor((date_format(ord_date,'%d')+(date_format(date_format(ord_date,'%Y%m%01'),'%w')-1))/7)+1 credate) " 
					+ "     ) mon " 
					+ "     right outer join " 
					+ "     (SELECT no as lmon FROM weekstatistics" 
					+ "     ) LS " 
					+ "     on y = lmon " 
					+ "     order by lmon";
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setInt(1, varWeekSelect);
			psmt.setString(2, id);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				WeekStatisticsDTO sdto = new WeekStatisticsDTO();
				sdto.setLmon(rs.getString("lmon"));
				sdto.setSumord(rs.getInt("sumord"));
				list.add(sdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	
	// 6. 판매 회원별 일 매출  sDayStatistics()
	public List<DayStatisticsDTO> sDayStatistics(String id, int varDaySelect) {
		List<DayStatisticsDTO> list = new ArrayList<DayStatisticsDTO>();
		String sql = "select concat(lmon,'일') as lmon, IFNULL(mon.sumord,0) as sumord " 
					+ "from (select DATE_FORMAT(ord_date, '%d') as y, sum(ord_detail_point) as sumOrd " 
					+ "        from ord o, ord_detail od,  product p " 
					+ "        where DATE_ADD(sysdate(), INTERVAL - 60 MONTH) < ord_date " 
					+ "        and DATE_FORMAT(ord_date, '%Y%m') = ? " 
					+ "        and sm_id = ? " 
					+ "        and o.ord_no = od.ord_no " 
					+ "        and o.pdt_no = p.pdt_no " 
					+ "        group by DATE_FORMAT(ord_date, '%d') " 
					+ "     ) mon " 
					+ "     right outer join " 
					+ "     (SELECT no as lmon FROM daystatistics" 
					+ "     ) LS " 
					+ "     on y = lmon " 
					+ "     order by lmon";
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setInt(1, varDaySelect);
			psmt.setString(2, id);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				DayStatisticsDTO dsdto = new DayStatisticsDTO();
				dsdto.setLmon(rs.getString("lmon"));
				dsdto.setSumord(rs.getInt("sumord"));
				list.add(dsdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	
	// 7. 판매 회원별 판매 품목 sProductStatistics()
	public List<pdtStatisticsDTO> sProductStatistics(String id) {
		List<pdtStatisticsDTO> list = new ArrayList<pdtStatisticsDTO>();
		String sql = "select pdt_kind_cd pdtKind, count(*) as pdtSt " 
					+ "from ord o, ord_detail od,  product p " 
					+ "where sm_id = ? " 
					+ "and o.ord_no = od.ord_no " 
					+ "and o.pdt_no = p.pdt_no " 
					+ "group by pdt_kind_cd ";
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, id);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				pdtStatisticsDTO psdto = new pdtStatisticsDTO();
				psdto.setPdtKind(rs.getString("pdtKind"));
				psdto.setPdtSt(rs.getInt("pdtSt"));
				
				list.add(psdto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
	
	// 8. 판매 회원별 구매한 성비 sSexStatistics()
	public List<SexRatioDTO> sSexStatistics(String id) {
		List<SexRatioDTO> list = new ArrayList<SexRatioDTO>();
		String sql = "select gender_cd g, count(*) as genSt " 
					+ "from ord o, ord_detail od,  product p " 
					+ "where sm_id = ? " 
					+ "and o.ord_no = od.ord_no " 
					+ "and o.pdt_no = p.pdt_no " 
					+ "group by gender_cd ";
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, id);
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				SexRatioDTO srdto = new SexRatioDTO();
				srdto.setGenSt(rs.getInt("genSt"));
				srdto.setGenderCd(rs.getString("g"));
				list.add(srdto);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return list;
	}
}
