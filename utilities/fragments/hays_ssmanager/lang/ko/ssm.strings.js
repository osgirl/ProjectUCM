
SSM.Strings['ssmError']                         = 'Site Studio 오류';
SSM.Strings['ssmWarning']                       = 'Site Studio 경고';
SSM.Strings['ssmInformation']                   = 'Site Studio 정보';
SSM.Strings['ssmRefresh']                       = '(새로 고침)';
SSM.Strings['ssmRefreshThisSite']               = '이 사이트 새로 고침';
SSM.Strings['ssmRemoveSection']                 = '섹션 제거';
SSM.Strings['ssmRemoveThisSection']             = '이 섹션 제거';
SSM.Strings['ssmRemoveSectionWarning']          = '"{1}" 섹션(ID: {2})을 영구적으로 삭제하려고 합니다. 이 작업을 수행하시겠습니까?';
SSM.Strings['ssmSectionLabelIsMissing']         = '섹션 레이블이 누락되었습니다.';
SSM.Strings['ssmInvalidCharacterIn']            = '{2}에 잘못된 문자 {1}이(가) 있습니다.';
SSM.Strings['ssmInvalidCharacterInUrl']         = 'URL에 잘못된 문자 {1}이(가) 있습니다.';
SSM.Strings['ssmInvalidCharacterInSectionId']   = '섹션 ID에 잘못된 문자 {1}이(가) 있습니다.';
SSM.Strings['ssmAddNewSection']                 = '새 섹션 추가';
SSM.Strings['ssmMoveSection']                   = '섹션 이동';
SSM.Strings['ssmMoveThisSection']               = '이 섹션 이동';
SSM.Strings['ssmMoveBeforeSection']             = '<i>"{1}"</i> 섹션 <b>앞</b>으로 이동';
SSM.Strings['ssmMoveAfterSection']              = '<i>"{1}"</i> 섹션 <b>뒤</b>로 이동';
SSM.Strings['ssmMoveIntoSection']               = '<i>"{1}"</i> 섹션 <b>내</b>로 이동';
SSM.Strings['ssmCancelMoveSection']             = '취소';
SSM.Strings['ssmIncludeSectionInNavigation']    = '탐색에 섹션 포함';
SSM.Strings['ssmSetSectionAsErrorHandler']      = '오류 처리기로 섹션 설정';
SSM.Strings['ssmEditingSection']                = ' # "{1}" 섹션 편집 중';
SSM.Strings['ssmEditingLayout']                 = ' # "{1}" 섹션의 레이아웃 편집 중';
SSM.Strings['ssmEditingSecondaryLayout']        = ' # "{1}" 섹션의 보조 레이아웃 편집 중';
SSM.Strings['ssmEditingCustomProperties']       = ' # "{1}" 섹션의 사용자 정의 등록 정보 편집 중';
SSM.Strings['ssmHome']                          = '홈';
SSM.Strings['ssmPrimary']                       = '기본';
SSM.Strings['ssmSecondary']                     = '보조';
SSM.Strings['ssmNotSet']                        = '설정 안 함';
SSM.Strings['ssmChooseExternalUrl']             = '<b>외부 URL 선택...</b>';
SSM.Strings['ssmChooseExternalUrlPrompt']       = '외부 URL 선택';
SSM.Strings['ssmUrlMustBeAbsolute']             = 'URL은 절대 http:// 또는 https:// URL이어야 합니다.';

SSM.Strings['ssmLoggedOnMessage']               = '<span class="ssm_logged_on_name">{1}</span>(으)로 <span class="ssm_logged_on">로그온했습니다.</span>';
SSM.Strings['ssmNotLoggedOnMessage']            = '로그온하지 않았습니다.';
SSM.Strings['ssmLoadSiteLinkLabel']             = '(사이트 관리)';
SSM.Strings['ssmHelpTitle']                     = '관리자 응용 프로그램에 대한 도움말 가져오기';

SSM.Strings['ssmHierarchyLabel']                = '사이트 계층 구조';

SSM.Strings['ssmSectionTabLabel']               = '섹션';
SSM.Strings['ssmLayoutTabLabel']                = '레이아웃';
SSM.Strings['ssmSecondaryLayoutTabLabel']       = '보조 레이아웃';
SSM.Strings['ssmCustomPropertiesTabLabel']      = '사용자 정의 등록 정보';

SSM.Strings['ssmActionsForSectionTitle']        = '<span id="ssm_actions_for_section_label"></span>&nbsp;섹션을 선택했습니다.';
SSM.Strings['ssmActionAddSection']              = '사이트의 이 섹션에 새 하위 섹션을 <a id="ssm_action_add_section_link" class="ssm_action_link" href="javascript:SSM.AddSection();" title="{1}">추가</a>합니다.';
SSM.Strings['ssmActionAddSectionTitle']         = '현재 선택한 섹션에 새 하위 섹션을 추가';
SSM.Strings['ssmActionRemoveSection']           = '사이트에서 이 섹션을 <a id="ssm_action_remove_section_link" class="ssm_action_link" href="javascript:SSM.RemoveSection();" title="{1}">제거</a>합니다.';
SSM.Strings['ssmActionRemoveSectionTitle']      = '현재 선택한 섹션 제거(루트 섹션은 제거할 수 없음)';
SSM.Strings['ssmActionMoveSection']             = '사이트의 다른 위치로 이 섹션을 <a id="ssm_action_move_section_link" class="ssm_action_link" href="javascript:SSM.MoveSection();" title="{1}">이동</a>합니다.';
SSM.Strings['ssmActionMoveSectionTitle']        = '현재 선택한 섹션을 계층 구조의 다른 위치로 이동';
SSM.Strings['ssmActionSetErrorHandler']         = '이 섹션을 사이트의 <a id="ssm_action_set_error_handler_link" class="ssm_action_link" href="javascript:SSM.SetErrorHandler();" title="{1}">오류 처리기</a>로 사용합니다.';
SSM.Strings['ssmActionSetErrorHandlerTitle']    = '이 섹션을 사이트의 오류 처리기로 사용(루트 섹션은 사용할 수 없음)';
SSM.Strings['ssmActionClearErrorHandler']       = '이 섹션을 사이트의 <a id="ssm_action_clear_error_handler_link" class="ssm_action_link" href="javascript:SSM.ClearErrorHandler();" title="{1}">오류 처리기</a>로 사용하지 않습니다.';
SSM.Strings['ssmActionClearErrorHandlerTitle']  = '이 섹션을 사이트의 오류 처리기로 사용 안 함';
SSM.Strings['ssmActionEditProperties']          = '이 섹션 아래의 등록 정보 편집...';
SSM.Strings['ssmSectionIdLabel']                = '섹션 ID:';
SSM.Strings['ssmSectionLabelLabel']             = '섹션 레이블:';
SSM.Strings['ssmIncludeInNavigationLabel']      = '탐색에 포함:';
SSM.Strings['ssmContributorOnlyLabel']          = 'Contributor 전용:';
SSM.Strings['ssmUrlDirectoryNameLabel']         = 'Url 디렉토리 이름:';
SSM.Strings['ssmUrlPageNameLabel']              = 'Url 페이지 이름:';
SSM.Strings['ssmMaximumAgeLabel']               = '최대 시간:';

SSM.Strings['ssmActionsForPrimaryLayoutTitle']  = '<span id="ssm_actions_for_primary_layout_label"></span>&nbsp;섹션을 선택했습니다.';
SSM.Strings['ssmHomeLayoutNameNotSet']          = '홈 레이아웃 페이지가 <span id="ssm_primary_layout_name">설정되지 않았습니다</span>.';
SSM.Strings['ssmHomeLayoutNameLabel']           = '홈 레이아웃 페이지가 <span id="ssm_primary_layout_name">{1}</span>입니다.';
SSM.Strings['ssmPrimaryLayoutNameNotSet']       = '기본 레이아웃 페이지가 <span id="ssm_primary_layout_name">설정되지 않았습니다</span>.';
SSM.Strings['ssmPrimaryLayoutNameLabel']        = '기본 레이아웃 페이지가 <span id="ssm_primary_layout_name">{1}</span>입니다.';
SSM.Strings['ssmSecondaryLayoutNameNotSet']     = '보조 레이아웃 페이지가 <span id="ssm_primary_layout_name">설정되지 않았습니다</span>.';
SSM.Strings['ssmSecondaryLayoutNameLabel']      = '보조 레이아웃 페이지가 <span id="ssm_primary_layout_name">{1}</span>입니다.';
SSM.Strings['ssmLayoutClearLabel']              = '지우기';
SSM.Strings['ssmLayoutClearTitle']              = '이 섹션과 연결된 레이아웃 제거';
SSM.Strings['ssmPreviewLayoutLabel']            = '레이아웃 미리보기';
SSM.Strings['ssmLayoutApplyLabel']              = '적용';
SSM.Strings['ssmLayoutApplyTitle']              = '이 섹션의 레이아웃 저장';

SSM.Strings['ssmActionsForSecondaryLayoutTitle']= '<span id="ssm_actions_for_secondary_layout_label"></span>&nbsp;섹션을 선택했습니다.';
SSM.Strings['ssmPreviewSecondaryLayoutLabel']   = '레이아웃 미리보기';
SSM.Strings['ssmSecondaryLayoutClearLabel']     = '지우기';
SSM.Strings['ssmSecondaryLayoutClearTitle']     = '이 섹션과 연결된 보조 레이아웃 제거';
SSM.Strings['ssmSecondaryLayoutApplyLabel']     = '적용';
SSM.Strings['ssmSecondaryLayoutApplyTitle']     = '이 섹션의 보조 레이아웃 저장';

SSM.Strings['ssmActionsForCustomPropertiesTitle']= '<span id="ssm_actions_for_custom_properties_label"></span>&nbsp;섹션을 선택했습니다.';
SSM.Strings['ssmActionEditCustomProperties']    = '이 섹션 아래의 사용자 정의 등록 정보 편집...';
SSM.Strings['ssmCustomPropertiesNameLabel']     = '사용자 정의 등록 정보';
SSM.Strings['ssmCustomPropertiesValueLabel']    = '값';

SSM.Strings['ssmMoveSectionNamePrefixLabel']    = '"<span id="ssm_form_move_section_label"></span>" 섹션 이동...';
SSM.Strings['ssmMoveSectionNameSuffixLabel']    = '...다음 섹션:';
SSM.Strings['ssmMoveSectionBeforeLabel']        = '이전';
SSM.Strings['ssmMoveSectionAfterLabel']         = '이후';
SSM.Strings['ssmMoveSectionInLabel']            = '하위 항목으로';

SSM.Strings['ssmAddNewSectionLabelLabel']       = '레이블:';
SSM.Strings['ssmAddNewSectionUrlDirNameLabel']  = 'URL:';
SSM.Strings['ssmAddNewSectionIdAutoLabel']      = '섹션 ID 자동 생성';
SSM.Strings['ssmAddNewSectionIdManualLabel']    = '섹션 ID 수동 입력';
SSM.Strings['ssmAddNewSectionAutoLabel']        = '(자동)';
